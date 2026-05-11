package com.wildtrail.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.wildtrail.app.WildTrailApp
import com.wildtrail.app.data.repository.AchievementRepository
import com.wildtrail.app.data.repository.AuthRepository
import com.wildtrail.app.data.repository.AuthState
import com.wildtrail.app.data.repository.HikeLogRepository
import com.wildtrail.app.data.repository.UserRepository
import com.wildtrail.app.domain.model.AchievementDefinition
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val hikes: List<HikeLog> = emptyList(),
    val earnedAchievements: List<AchievementDefinition> = emptyList(),
    /** True when the displayed profile is the logged-in user. Drives sign-out
     *  visibility, settings, etc. */
    val isMe: Boolean = true,
)

/**
 * @param targetUid The UID of the user to display. `null` means "the
 *                  currently-logged-in user" — picked from authState.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val targetUid: String?,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val hikeLogRepository: HikeLogRepository,
    private val achievementRepository: AchievementRepository,
) : ViewModel() {

    init {
        viewModelScope.launch { runCatching { achievementRepository.syncDefinitions() } }
    }

    /** Resolved UID flow: explicit target if provided, else current user's. */
    private val uidFlow = if (targetUid != null) {
        flowOf(targetUid)
    } else {
        authRepository.authState.map { (it as? AuthState.SignedIn)?.user?.firebaseUid }
    }

    val uiState: StateFlow<ProfileUiState> = uidFlow
        .flatMapLatest { uid ->
            if (uid == null) {
                flowOf(ProfileUiState())
            } else {
                combine(
                    userRepository.observeUser(uid),
                    hikeLogRepository.observeMyHikes(uid),
                    achievementRepository.observeEarned(uid),
                    authRepository.authState,
                ) { user, hikes, achievements, auth ->
                    val meUid = (auth as? AuthState.SignedIn)?.user?.firebaseUid
                    ProfileUiState(
                        user = user,
                        hikes = hikes,
                        earnedAchievements = achievements,
                        isMe = meUid != null && meUid == uid,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProfileUiState(),
        )

    fun signOut() = authRepository.signOut()

    fun refresh() {
        viewModelScope.launch { runCatching { hikeLogRepository.refresh() } }
    }

    fun updateProfile(bio: String?, country: String?) {
        val current = uiState.value.user ?: return
        viewModelScope.launch {
            runCatching {
                userRepository.updateUser(current.copy(bio = bio, country = country))
            }
        }
    }

    companion object {
        fun factory(targetUid: String? = null): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WildTrailApp)
                ProfileViewModel(
                    targetUid = targetUid,
                    authRepository = app.container.authRepository,
                    userRepository = app.container.userRepository,
                    hikeLogRepository = app.container.hikeLogRepository,
                    achievementRepository = app.container.achievementRepository,
                )
            }
        }
    }
}
