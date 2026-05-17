package com.wildtrail.app.ui.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.wildtrail.app.WildTrailApp
import com.wildtrail.app.data.repository.AuthRepository
import com.wildtrail.app.data.repository.AuthState
import com.wildtrail.app.data.repository.HikeLogRepository
import com.wildtrail.app.data.repository.UserRepository
import com.wildtrail.app.domain.model.GeoPoint
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.SurfaceType
import com.wildtrail.app.util.HikeMath
import com.wildtrail.app.util.LocationTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * UI state for the live-tracking screen.
 *
 *  - [routePoints]: the trace as we've collected it so far. Drawn as a
 *                   polyline on the map and used to compute distance / gain.
 *  - [status]:      finite-state machine for IDLE → RECORDING → PAUSED → STOPPED.
 */
data class TrackingUiState(
    val status: TrackingStatus = TrackingStatus.IDLE,
    val routePoints: List<GeoPoint> = emptyList(),
    val distanceKm: Float = 0f,
    val elevationGainM: Int = 0,
    val durationSec: Long = 0L,
    val avgSpeedKmh: Float = 0f,
    val hasPermission: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
    /** Latest GPS fix, tracked even before "Start hike" so the map can
     *  center on the user instead of showing a "waiting for GPS" box. */
    val currentLocation: GeoPoint? = null,
)

enum class TrackingStatus { IDLE, RECORDING, PAUSED, STOPPED }

class TrackingViewModel(
    private val locationTracker: LocationTracker,
    private val hikeLogRepository: HikeLogRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    /** Current location-collection job; cancelled on pause/stop. */
    private var locationJob: Job? = null

    /** Lightweight location collection that runs *before* recording so the
     *  map has a position to show. Cancelled once real recording starts. */
    private var previewJob: Job? = null
    private var startTimeMs: Long = 0L
    private var elapsedBeforePauseMs: Long = 0L

    init {
        val granted = locationTracker.hasLocationPermission()
        _uiState.update { it.copy(hasPermission = granted) }
        if (granted) startLocationPreview()
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasPermission = granted) }
        if (granted) startLocationPreview()
    }

    /**
     * Stream the user's position while we're idle so the map can center on
     * them immediately. Only touches [TrackingUiState.currentLocation] — the
     * recorded route is owned by [beginCollecting]; this never adds points.
     */
    private fun startLocationPreview() {
        if (previewJob?.isActive == true) return
        previewJob = viewModelScope.launch {
            try {
                locationTracker.observeLocation(intervalMs = 3_000L).collect { point ->
                    _uiState.update { st ->
                        if (st.status == TrackingStatus.IDLE ||
                            st.status == TrackingStatus.STOPPED
                        ) {
                            st.copy(currentLocation = point)
                        } else {
                            st
                        }
                    }
                }
            } catch (e: SecurityException) {
                _uiState.update { it.copy(errorMessage = e.message, hasPermission = false) }
            }
        }
    }

    fun start() {
        if (!locationTracker.hasLocationPermission()) {
            _uiState.update { it.copy(errorMessage = "Location permission required") }
            return
        }
        startTimeMs = System.currentTimeMillis()
        elapsedBeforePauseMs = 0L
        // The recording collector takes over location; stop the idle preview.
        previewJob?.cancel()
        _uiState.update { it.copy(status = TrackingStatus.RECORDING, routePoints = emptyList(), errorMessage = null) }
        beginCollecting()
        beginTimer()
    }

    fun pause() {
        if (_uiState.value.status != TrackingStatus.RECORDING) return
        elapsedBeforePauseMs += System.currentTimeMillis() - startTimeMs
        locationJob?.cancel()
        _uiState.update { it.copy(status = TrackingStatus.PAUSED) }
    }

    fun resume() {
        if (_uiState.value.status != TrackingStatus.PAUSED) return
        startTimeMs = System.currentTimeMillis()
        _uiState.update { it.copy(status = TrackingStatus.RECORDING) }
        beginCollecting()
        beginTimer()
    }

    fun stop() {
        locationJob?.cancel()
        if (_uiState.value.status == TrackingStatus.RECORDING) {
            elapsedBeforePauseMs += System.currentTimeMillis() - startTimeMs
        }
        _uiState.update { it.copy(status = TrackingStatus.STOPPED) }
    }

    /**
     * Persist the recorded hike. The "characteristics" (difficulty, mud, …)
     * are filled in by the creator at save time and live on the [HikeLog]
     * itself; *other* users then leave [com.wildtrail.app.domain.model.TrailReview]s
     * whose `overallRating`s are averaged into [HikeLog.averageRating].
     */
    fun saveHike(
        title: String,
        description: String?,
        surfaceType: SurfaceType,
        isPrivate: Boolean,
        difficultyLevel: Int,
        mudRisk: Int,
        pathClarity: Int,
        fatigueLevel: Int,
        animalEncounterRisk: Int,
        waterAvailability: Boolean,
    ) {
        val state = _uiState.value
        val auth = authRepository.authState.value
        if (auth !is AuthState.SignedIn) {
            _uiState.update { it.copy(errorMessage = "You must be signed in to save a hike") }
            return
        }
        val now = System.currentTimeMillis()
        val durationSec = state.durationSec
        viewModelScope.launch {
            // authState.user is a snapshot captured at sign-in (it never
            // re-emits on Room changes), so a profile picture set/changed
            // afterwards isn't on it. Read the live Room user so the picture
            // is denormalised onto the hike and shows on every card preview.
            val creator = runCatching { userRepository.getUser(auth.user.firebaseUid) }
                .getOrNull() ?: auth.user
            val hike = HikeLog(
            hikeId = UUID.randomUUID().toString(),
            creatorFirebaseUid = creator.firebaseUid,
            creatorUsername = creator.username,
            creatorProfilePictureUrl = creator.profilePictureUrl,
            workoutId = null,
            title = title,
            description = description,
            avgSpeedKmh = state.avgSpeedKmh,
            stepCount = 0,
            caloriesBurned = HikeMath.estimateCalories(state.distanceKm, state.elevationGainM),
            coverPhotoUrl = null,
            xpEarned = HikeMath.xpFromHike(state.distanceKm, state.elevationGainM),
            likesCount = 0,
            surfaceType = surfaceType,
            lengthKm = state.distanceKm,
            durationSeconds = durationSec,
            startedAt = now - durationSec * 1000L,
            endedAt = now,
            elevationGainMeters = state.elevationGainM,
            routeCoordinates = state.routePoints,
            isPrivate = isPrivate,
            difficultyLevel = difficultyLevel,
            mudRisk = mudRisk,
            pathClarity = pathClarity,
            fatigueLevel = fatigueLevel,
            animalEncounterRisk = animalEncounterRisk,
            waterAvailability = waterAvailability,
            averageRating = 0f,
            reviewCount = 0,
            )
            runCatching {
                hikeLogRepository.saveHike(hike)
                // Bump the cached user totals so the Profile screen reflects
                // the new hike count / XP / level immediately. Wrapped in a
                // separate runCatching so a failure here can't cause the
                // user to see "Could not save hike" when the hike *was* saved.
                runCatching {
                    userRepository.incrementHikeStats(
                        uid = creator.firebaseUid,
                        distanceKm = hike.lengthKm,
                        xpEarned = hike.xpEarned,
                    )
                }
            }
                .onSuccess { _uiState.update { it.copy(saved = true) } }
                .onFailure { err ->
                    _uiState.update { it.copy(errorMessage = err.message ?: "Could not save hike") }
                }
        }
    }

    fun resetAfterSave() {
        val granted = locationTracker.hasLocationPermission()
        _uiState.value = TrackingUiState(hasPermission = granted)
        if (granted) startLocationPreview()
    }

    private fun beginCollecting() {
        locationJob = viewModelScope.launch {
            try {
                locationTracker.observeLocation(intervalMs = 2_000L).collect { point ->
                    _uiState.update { state ->
                        val newRoute = state.routePoints + point
                        val distanceKm = HikeMath.totalDistanceKm(newRoute)
                        val gain = HikeMath.elevationGainMeters(newRoute)
                        state.copy(
                            routePoints = newRoute,
                            currentLocation = point,
                            distanceKm = distanceKm,
                            elevationGainM = gain,
                            avgSpeedKmh = HikeMath.avgSpeedKmh(distanceKm, state.durationSec),
                        )
                    }
                }
            } catch (e: SecurityException) {
                _uiState.update { it.copy(errorMessage = e.message, hasPermission = false) }
            }
        }
    }

    private fun beginTimer() {
        viewModelScope.launch {
            while (isActive && _uiState.value.status == TrackingStatus.RECORDING) {
                kotlinx.coroutines.delay(1_000L)
                _uiState.update { state ->
                    val totalMs = elapsedBeforePauseMs + (System.currentTimeMillis() - startTimeMs)
                    val durSec = totalMs / 1_000L
                    state.copy(
                        durationSec = durSec,
                        avgSpeedKmh = HikeMath.avgSpeedKmh(state.distanceKm, durSec),
                    )
                }
            }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WildTrailApp)
                TrackingViewModel(
                    locationTracker = app.container.locationTracker,
                    hikeLogRepository = app.container.hikeLogRepository,
                    userRepository = app.container.userRepository,
                    authRepository = app.container.authRepository,
                )
            }
        }
    }
}
