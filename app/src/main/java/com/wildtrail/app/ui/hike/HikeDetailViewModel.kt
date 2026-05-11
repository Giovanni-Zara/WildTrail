package com.wildtrail.app.ui.hike

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.wildtrail.app.WildTrailApp
import com.wildtrail.app.data.repository.AuthRepository
import com.wildtrail.app.data.repository.AuthState
import com.wildtrail.app.data.repository.HikeLogRepository
import com.wildtrail.app.data.repository.SocialRepository
import com.wildtrail.app.data.repository.UserRepository
import com.wildtrail.app.domain.model.HikeComment
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.TrailReview
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
import java.util.UUID

data class HikeDetailUiState(
    val hike: HikeLog? = null,
    val creator: User? = null,
    val reviews: List<TrailReview> = emptyList(),
    val comments: List<HikeComment> = emptyList(),
    /** uid -> User cache populated by [observeAuthorsFor], used to render
     *  reviewer / commenter usernames + clickable rows. */
    val authors: Map<String, User> = emptyMap(),
    val likeCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val currentUserUid: String? = null,
    val isMyHike: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HikeDetailViewModel(
    private val hikeId: String,
    private val hikeLogRepository: HikeLogRepository,
    private val socialRepository: SocialRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    /** UID-of-current-user as a flow. */
    private val currentUidFlow = authRepository.authState.map {
        (it as? AuthState.SignedIn)?.user?.firebaseUid
    }

    /** The hike, plus its creator user (looked up from UserRepository). */
    private val hikeWithCreator = hikeLogRepository.observeHike(hikeId)
        .flatMapLatest { hike ->
            if (hike == null) flowOf(null to null)
            else userRepository.observeUser(hike.creatorFirebaseUid).map { hike to it }
        }

    private val likes = currentUidFlow.flatMapLatest { uid ->
        if (uid == null) flowOf(false to 0)
        else combine(
            hikeLogRepository.observeIsLiked(uid, hikeId),
            hikeLogRepository.observeLikeCount(hikeId),
        ) { liked, count -> liked to count }
    }

    /**
     * Reactive flow of (uid -> User) for everyone we currently render — the
     * reviewers + commenters. We collect the union of UIDs from both lists,
     * and observe each user from Room (cheap; Room caches everything).
     */
    private val reviews = socialRepository.observeReviewsForHike(hikeId)
    private val comments = socialRepository.observeComments(hikeId)

    private val authorMap = combine(reviews, comments) { rs, cs ->
        (rs.map { it.reviewerUid } + cs.map { it.authorUid }).toSet()
    }.flatMapLatest { uids ->
        if (uids.isEmpty()) flowOf(emptyMap())
        else combine(uids.map { uid -> userRepository.observeUser(uid).map { uid to it } }) {
            it.toMap().mapValues { (_, v) -> v }.filterValues { it != null }
                .mapValues { (_, v) -> v!! }
        }
    }

    val uiState: StateFlow<HikeDetailUiState> = combine(
        hikeWithCreator,
        reviews,
        comments,
        likes,
        currentUidFlow,
    ) { hikeAndCreator, rs, cs, (liked, count), uid ->
        val (hike, creator) = hikeAndCreator
        HikeDetailUiState(
            hike = hike,
            creator = creator,
            reviews = rs,
            comments = cs,
            likeCount = count,
            isLikedByMe = liked,
            currentUserUid = uid,
            isMyHike = hike != null && uid != null && hike.creatorFirebaseUid == uid,
        )
    }
        .combine(authorMap) { state, authors -> state.copy(authors = authors) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HikeDetailUiState(),
        )

    fun submitReview(
        overallRating: Int,
        fatigueLevel: Int,
        pathClarity: Int,
        difficultyLevel: Int,
        mudRisk: Int,
        animalEncounterRisk: Int,
        waterAvailability: Boolean,
    ) {
        val auth = authRepository.authState.value as? AuthState.SignedIn ?: return
        // The creator can't review their own hike; UI already guards this,
        // we double-check in the ViewModel.
        if (uiState.value.isMyHike) return
        val review = TrailReview(
            reviewId = UUID.randomUUID().toString(),
            reviewerUid = auth.user.firebaseUid,
            hikeId = hikeId,
            overallRating = overallRating,
            fatigueLevel = fatigueLevel,
            pathClarity = pathClarity,
            difficultyLevel = difficultyLevel,
            mudRisk = mudRisk,
            animalEncounterRisk = animalEncounterRisk,
            waterAvailability = waterAvailability,
            createdAt = System.currentTimeMillis(),
        )
        viewModelScope.launch {
            runCatching {
                socialRepository.submitReview(review)
                // Recompute the hike's averageRating + reviewCount so cards
                // and the detail header refresh immediately.
                hikeLogRepository.refreshAggregateRating(hikeId)
            }
        }
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

    fun toggleLike() {
        val state = uiState.value
        val uid = state.currentUserUid ?: return
        viewModelScope.launch {
            runCatching { hikeLogRepository.setLiked(uid, hikeId, !state.isLikedByMe) }
        }
    }

    fun refresh() {
        viewModelScope.launch { runCatching { hikeLogRepository.refresh() } }
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
                    userRepository = app.container.userRepository,
                    authRepository = app.container.authRepository,
                )
            }
        }
    }
}
