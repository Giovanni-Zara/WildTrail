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
    val isMe: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val hikeLogRepository: HikeLogRepository,
    private val achievementRepository: AchievementRepository,
) : ViewModel() {

    init {
        // Pull definitions once on first construction.
        viewModelScope.launch { runCatching { achievementRepository.syncDefinitions() } }
    }

    val uiState: StateFlow<ProfileUiState> = authRepository.authState
        .flatMapLatest { auth ->
            val uid = (auth as? AuthState.SignedIn)?.user?.firebaseUid
            if (uid == null) {
                flowOf(ProfileUiState())
            } else {
                combine(
                    userRepository.observeUser(uid),
                    hikeLogRepository.observeMyHikes(uid),
                    achievementRepository.observeEarned(uid),
                ) { user, hikes, achievements ->
                    ProfileUiState(
                        user = user,
                        hikes = hikes,
                        earnedAchievements = achievements,
                        isMe = true,
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

    fun updateProfile(bio: String?, country: String?, age: Int?) {
        val current = uiState.value.user ?: return
        viewModelScope.launch {
            runCatching {
                userRepository.updateUser(
                    current.copy(bio = bio, country = country, age = age),
                )
            }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WildTrailApp)
                ProfileViewModel(
                    authRepository = app.container.authRepository,
                    userRepository = app.container.userRepository,
                    hikeLogRepository = app.container.hikeLogRepository,
                    achievementRepository = app.container.achievementRepository,
                )
            }
        }
    }
}
