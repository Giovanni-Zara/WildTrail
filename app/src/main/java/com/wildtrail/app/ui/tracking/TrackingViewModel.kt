package com.wildtrail.app.ui.tracking

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.wildtrail.app.BuildConfig
import com.wildtrail.app.WildTrailApp
import com.wildtrail.app.data.repository.AuthRepository
import com.wildtrail.app.data.repository.AuthState
import com.wildtrail.app.data.repository.EmergencyContactRepository
import com.wildtrail.app.data.repository.HikeLogRepository
import com.wildtrail.app.data.repository.UserRepository
import com.wildtrail.app.domain.model.GeoPoint
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.HikeMediaItem
import com.wildtrail.app.domain.model.HikeMediaType
import com.wildtrail.app.domain.model.SurfaceType
import com.wildtrail.app.domain.usecase.DetectFallUseCase
import com.wildtrail.app.domain.usecase.FallDetectionEvent
import com.wildtrail.app.util.AudioRecorder
import com.wildtrail.app.util.HikeMath
import com.wildtrail.app.util.HikeMediaStore
import com.wildtrail.app.util.LocationTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    /** Photos / voice notes captured during the current session. Persisted
     *  onto the hike when the user saves; cleared if they discard. */
    val mediaItems: List<HikeMediaItem> = emptyList(),
    /** True while [AudioRecorder] is active — drives the UI button state. */
    val isRecordingAudio: Boolean = false,
    /** Non-null while the fall-detection emergency overlay is showing. Source
     *  of truth for the overlay (survives recomposition / rotation) and the
     *  gate that keeps fall detection suppressed until the user resumes. */
    val emergency: EmergencyUiState? = null,
)

/** State backing the full-screen emergency overlay shown after a fall. */
data class EmergencyUiState(
    /** Who the placeholder emergency call is addressed to (the user's primary
     *  fall-notify [com.wildtrail.app.domain.model.EmergencyContact], or a
     *  generic fallback label). */
    val contactName: String,
    /** Countdown length before the call is auto-placed. */
    val countdownSeconds: Int = EMERGENCY_COUNTDOWN_SECONDS,
)

enum class TrackingStatus { IDLE, RECORDING, PAUSED, STOPPED }

/** Default emergency countdown, exposed so the overlay and VM agree. */
const val EMERGENCY_COUNTDOWN_SECONDS = 15

class TrackingViewModel(
    private val locationTracker: LocationTracker,
    private val hikeLogRepository: HikeLogRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val mediaStore: HikeMediaStore,
    private val audioRecorder: AudioRecorder,
    private val detectFallUseCase: DetectFallUseCase,
    private val emergencyContactRepository: EmergencyContactRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    /** One-shot UI events (the spec's "NavigationEvent" channel). Buffered so
     *  an emit never blocks the VM even if the UI isn't collecting yet. */
    private val _events = Channel<TrackingEvent>(Channel.BUFFERED)
    val events: Flow<TrackingEvent> = _events.receiveAsFlow()

    /** Current location-collection job; cancelled on pause/stop. */
    private var locationJob: Job? = null

    /** Fall-detection collection job. Launched only while RECORDING and
     *  cancelled the moment we leave it — that gate is what stops a second
     *  fall from firing while the emergency overlay is up. */
    private var fallDetectionJob: Job? = null

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
        startFallDetection()
    }

    fun pause() {
        if (_uiState.value.status != TrackingStatus.RECORDING) return
        elapsedBeforePauseMs += System.currentTimeMillis() - startTimeMs
        locationJob?.cancel()
        stopFallDetection()
        _uiState.update { it.copy(status = TrackingStatus.PAUSED) }
    }

    fun resume() {
        if (_uiState.value.status != TrackingStatus.PAUSED) return
        // Don't resume into recording while the emergency overlay is still up.
        if (_uiState.value.emergency != null) return
        startTimeMs = System.currentTimeMillis()
        _uiState.update { it.copy(status = TrackingStatus.RECORDING) }
        beginCollecting()
        beginTimer()
        startFallDetection()
    }

    fun stop() {
        locationJob?.cancel()
        stopFallDetection()
        if (_uiState.value.status == TrackingStatus.RECORDING) {
            elapsedBeforePauseMs += System.currentTimeMillis() - startTimeMs
        }
        _uiState.update { it.copy(status = TrackingStatus.STOPPED) }
    }

    // ----------------------- Fall detection ----------------------------------

    /**
     * Begin collecting [DetectFallUseCase]. The activation gate is structural:
     * this is only ever called when entering RECORDING, and [stopFallDetection]
     * is called the instant we leave it (pause / stop / fall), so the use case
     * is never collected outside an active recording.
     */
    private fun startFallDetection() {
        if (fallDetectionJob?.isActive == true) return
        Log.d(TAG, "Fall detection ACTIVE (recording started)")
        fallDetectionJob = viewModelScope.launch {
            detectFallUseCase().collect { event ->
                when (event) {
                    is FallDetectionEvent.FallDetected -> onFallDetected()
                }
            }
        }
    }

    private fun stopFallDetection() {
        if (fallDetectionJob != null) Log.d(TAG, "Fall detection stopped")
        fallDetectionJob?.cancel()
        fallDetectionJob = null
    }

    /**
     * Fall confirmed. Pause the trek (timer + GPS), then raise the emergency
     * overlay. We leave RECORDING *synchronously* via [pause] first so the
     * activation gate immediately suppresses any further fall events.
     */
    private fun onFallDetected() {
        if (_uiState.value.status != TrackingStatus.RECORDING) return
        Log.w(TAG, "onFallDetected(): fall confirmed by sensors")
        pause()
        raiseEmergency()
    }

    /**
     * Debug-only entry point (wired to a hidden button in debug builds) that
     * raises the emergency overlay without a real fall — lets you verify the
     * haptics / voice prompt / countdown / swipe-to-cancel end to end.
     */
    fun simulateFall() {
        Log.w(TAG, "simulateFall(): manually raising emergency overlay (debug)")
        if (_uiState.value.status == TrackingStatus.RECORDING) pause()
        raiseEmergency()
    }

    /**
     * Resolve the contact and show the overlay. The lookup + state update run
     * in a coroutine that is a child of [viewModelScope] (not the
     * fall-collection job), so it survives that job's cancellation in [pause].
     */
    private fun raiseEmergency() {
        if (_uiState.value.emergency != null) return
        viewModelScope.launch {
            val contactName = resolveEmergencyContactName()
            _uiState.update { it.copy(emergency = EmergencyUiState(contactName = contactName)) }
            _events.send(TrackingEvent.ShowEmergencyOverlay)
        }
    }

    /** Swipe-to-cancel on the overlay: abort the emergency. Per spec the trek
     *  stays PAUSED — the user must explicitly resume. */
    fun onEmergencyCancelled() {
        _uiState.update { it.copy(emergency = null) }
    }

    /** Countdown reached zero with no cancellation → place the (placeholder)
     *  emergency call and dismiss the overlay (trek stays PAUSED). */
    fun onEmergencyCountdownFinished() {
        val contactName = _uiState.value.emergency?.contactName ?: return
        initiateEmergencyCall(contactName)
        _uiState.update { it.copy(emergency = null) }
    }

    /**
     * Placeholder for the real telephony action (deliberately **not**
     * `ACTION_CALL`). For now it logs and asks the UI to show a Toast; the
     * real `Intent` is wired up later.
     */
    private fun initiateEmergencyCall(contactName: String) {
        Log.w(TAG, "initiateEmergencyCall(): emergency call would be placed to '$contactName'")
        viewModelScope.launch { _events.send(TrackingEvent.EmergencyCallPlaced(contactName)) }
    }

    private suspend fun resolveEmergencyContactName(): String {
        val uid = (authRepository.authState.value as? AuthState.SignedIn)?.user?.firebaseUid
            ?: return DEFAULT_CONTACT_LABEL
        val contacts = runCatching { emergencyContactRepository.getFallNotifyList(uid) }
            .getOrDefault(emptyList())
        val chosen = contacts.firstOrNull { it.isPrimary } ?: contacts.firstOrNull()
        return chosen?.name ?: DEFAULT_CONTACT_LABEL
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
            mediaItems = state.mediaItems,
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
        // If the user discarded mid-recording with an active audio capture,
        // make sure the MediaRecorder is released so the mic isn't held.
        if (_uiState.value.isRecordingAudio) runCatching { audioRecorder.stop() }
        stopFallDetection()
        val granted = locationTracker.hasLocationPermission()
        _uiState.value = TrackingUiState(hasPermission = granted)
        if (granted) startLocationPreview()
    }

    // ---------------- Sensor capture: photos & audio --------------------

    /**
     * Persist a photo taken from the system camera and tag it with the user's
     * current GPS position so it can be pinned on the map when the hike is
     * later viewed.
     */
    fun capturePhoto(bitmap: Bitmap) {
        val location = _uiState.value.currentLocation ?: run {
            _uiState.update { it.copy(errorMessage = "No GPS fix yet — try again in a moment") }
            return
        }
        viewModelScope.launch {
            runCatching {
                val file = mediaStore.savePhoto(bitmap)
                addMediaItem(HikeMediaType.PHOTO, file.absolutePath, location)
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Could not save photo") }
            }
        }
    }

    /** Begin recording audio from the device microphone. */
    fun startAudioRecording() {
        if (_uiState.value.isRecordingAudio) return
        val target = mediaStore.newAudioFile()
        runCatching { audioRecorder.start(target) }
            .onSuccess { _uiState.update { it.copy(isRecordingAudio = true) } }
            .onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Could not start recording") }
            }
    }

    /** Stop the in-flight audio recording and tag the resulting file. */
    fun stopAudioRecording() {
        if (!_uiState.value.isRecordingAudio) return
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) { audioRecorder.stop() }
            _uiState.update { it.copy(isRecordingAudio = false) }
            val location = _uiState.value.currentLocation
            if (file != null && location != null) {
                addMediaItem(HikeMediaType.AUDIO, file.absolutePath, location)
            }
        }
    }

    private fun addMediaItem(type: HikeMediaType, path: String, location: GeoPoint) {
        val item = HikeMediaItem(
            id = UUID.randomUUID().toString(),
            type = type,
            filePath = path,
            lat = location.lat,
            lng = location.lng,
            timestamp = System.currentTimeMillis(),
        )
        _uiState.update { it.copy(mediaItems = it.mediaItems + item) }
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
        private const val TAG = "TrackingViewModel"

        /** Fallback when the user has configured no fall-notify contact. */
        private const val DEFAULT_CONTACT_LABEL = "Emergency Services"

        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WildTrailApp)
                TrackingViewModel(
                    locationTracker = app.container.locationTracker,
                    hikeLogRepository = app.container.hikeLogRepository,
                    userRepository = app.container.userRepository,
                    authRepository = app.container.authRepository,
                    mediaStore = app.container.hikeMediaStore,
                    audioRecorder = AudioRecorder(app.applicationContext),
                    detectFallUseCase = DetectFallUseCase(
                        sensorRepository = app.container.sensorRepository,
                        debugLog = if (BuildConfig.DEBUG) {
                            { msg -> Log.d("FallDetection", msg) }
                        } else {
                            null
                        },
                    ),
                    emergencyContactRepository = app.container.emergencyContactRepository,
                )
            }
        }
    }
}
