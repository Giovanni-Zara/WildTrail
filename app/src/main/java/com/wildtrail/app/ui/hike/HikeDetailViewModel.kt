package com.wildtrail.app.ui.hike

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.wildtrail.app.WildTrailApp
import com.wildtrail.app.data.repository.AuthRepository
import com.wildtrail.app.data.repository.AuthState
import com.wildtrail.app.data.repository.HikeLogRepository
import com.wildtrail.app.data.repository.SocialRepository
import com.wildtrail.app.domain.model.HikeComment
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.TrailReview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class HikeDetailUiState(
    val hike: HikeLog? = null,
    val reviews: List<TrailReview> = emptyList(),
    val comments: List<HikeComment> = emptyList(),
    val isFollowingTrail: Boolean = false,
)

class HikeDetailViewModel(
    private val hikeId: String,
    private val hikeLogRepository: HikeLogRepository,
    private val socialRepository: SocialRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val uiState: StateFlow<HikeDetailUiState> = combine(
        hikeLogRepository.observeHike(hikeId),
        socialRepository.observeReviewsForHike(hikeId),
        socialRepository.observeComments(hikeId),
    ) { hike, reviews, comments ->
        HikeDetailUiState(
            hike = hike,
            reviews = reviews,
            comments = comments,
            isFollowingTrail = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HikeDetailUiState(),
    )

    fun submitReview(
        fatigueLevel: Int,
        pathClarity: Int,
        difficultyLevel: Int,
        mudRisk: Int,
        animalEncounterRisk: Int,
        waterAvailability: Boolean,
    ) {
        val auth = authRepository.authState.value as? AuthState.SignedIn ?: return
        val review = TrailReview(
            reviewId = UUID.randomUUID().toString(),
            reviewerUid = auth.user.firebaseUid,
            hikeId = hikeId,
            fatigueLevel = fatigueLevel,
            pathClarity = pathClarity,
            difficultyLevel = difficultyLevel,
            mudRisk = mudRisk,
            animalEncounterRisk = animalEncounterRisk,
            waterAvailability = waterAvailability,
            createdAt = System.currentTimeMillis(),
        )
        viewModelScope.launch { runCatching { socialRepository.submitReview(review) } }
    }

    fun postComment(text: String) {
        val auth = authRepository.authState.value as? AuthState.SignedIn ?: return
        val comment = HikeComment(
            commentId = UUID.randomUUID().toString(),
            authorUid = auth.user.firebaseUid,
            hikeId = hikeId,
            text = text,
            photoUrls = emptyList(),
            createdAt = System.currentTimeMillis(),
        )
        viewModelScope.launch { runCatching { socialRepository.postComment(comment) } }
    }

    companion object {
        const val ARG_HIKE_ID = "hikeId"

        fun factory(hikeId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WildTrailApp)
                HikeDetailViewModel(
                    hikeId = hikeId,
                    hikeLogRepository = app.container.hikeLogRepository,
                    socialRepository = app.container.socialRepository,
                    authRepository = app.container.authRepository,
                )
            }
        }
    }
}
