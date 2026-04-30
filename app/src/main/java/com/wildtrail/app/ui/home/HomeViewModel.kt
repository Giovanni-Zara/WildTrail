package com.wildtrail.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.wildtrail.app.WildTrailApp
import com.wildtrail.app.data.repository.AuthRepository
import com.wildtrail.app.data.repository.AuthState
import com.wildtrail.app.data.repository.HikeLogRepository
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val currentUser: User? = null,
    val recentHikes: List<HikeLog> = emptyList(),
    val publicFeed: List<HikeLog> = emptyList(),
    val isOffline: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val hikeLogRepository: HikeLogRepository,
) : ViewModel() {

    private val publicFeed: Flow<List<HikeLog>> =
        hikeLogRepository.observePublicFeed(20)

    /**
     * `flatMapLatest` is the idiomatic way to "switch" a flow when an
     * upstream value changes — exactly what we need when the logged-in user
     * changes and we have to re-subscribe to *their* hikes.
     */
    private val myHikes: Flow<List<HikeLog>> = authRepository.authState
        .map { (it as? AuthState.SignedIn)?.user?.firebaseUid }
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList()) else hikeLogRepository.observeMyHikes(uid)
        }

    /**
     * `combine` merges three independent flows into one [StateFlow] — every
     * time any of them emits, the UI gets a fresh snapshot. `stateIn`
     * upgrades it to a hot StateFlow with a 5-second sharing timeout, so it
     * survives short config changes without restarting upstream collections
     * (avoids the "double-fetch on rotation" anti-pattern).
     */
    val uiState: StateFlow<HomeUiState> = combine(
        authRepository.authState,
        publicFeed,
        myHikes,
    ) { auth, publicHikes, mine ->
        HomeUiState(
            currentUser = (auth as? AuthState.SignedIn)?.user,
            recentHikes = mine,
            publicFeed = publicHikes,
            isOffline = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeUiState(),
    )

    fun signOut() = authRepository.signOut()

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WildTrailApp)
                HomeViewModel(
                    authRepository = app.container.authRepository,
                    hikeLogRepository = app.container.hikeLogRepository,
                )
            }
        }
    }
}
