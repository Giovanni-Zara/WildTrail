package com.wildtrail.app.ui.hike

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.wildtrail.app.WildTrailApp
import com.wildtrail.app.data.repository.AuthRepository
import com.wildtrail.app.data.repository.AuthState
import com.wildtrail.app.data.repository.PredictRepository
import com.wildtrail.app.data.repository.HikeLogRepository
import com.wildtrail.app.data.repository.SocialRepository
import com.wildtrail.app.data.repository.UserRepository
import com.wildtrail.app.domain.model.HikeComment
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.TrailReview
import com.wildtrail.app.domain.model.User
import com.wildtrail.app.domain.usecase.GetReviewSummaryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Represents every possible state of the AI time prediction.
 *
 * Why a sealed interface?
 *   It forces the UI to handle every case explicitly (like a type-safe switch).
 *   The `when` expression in Compose won't compile unless all branches are covered.
 *
 * State machine:
 *   Idle → (button pressed) → Loading → Success(minutes)
 *                                      → Error(message) → (retry) → Loading
 *
 * The state resets to Idle each time the user navigates away and back,
 * because the ViewModel is recreated with each new navigation back-stack entry.
 */
sealed interface PredictionState {
    data object Idle : PredictionState
    data object Loading : PredictionState
    data class Success(val minutes: Double) : PredictionState
    data class Error(val message: String) : PredictionState
}

/**
 * State of the on-demand AI review summary. Like [PredictionState] it starts
 * Idle, and because the ViewModel is recreated on each navigation to the
 * screen, the summary auto-disposes when the user leaves the hike.
 */
sealed interface ReviewSummaryState {
    data object Idle : ReviewSummaryState
    data object Loading : ReviewSummaryState
    data class Success(val summary: String) : ReviewSummaryState
    data class Error(val message: String) : ReviewSummaryState
}

data class HikeDetailUiState(
    /** Distinct from `hike == null` so the UI can tell "still loading" from
     *  "loaded but the hike doesn't exist". */
    val loading: Boolean = true,
    val hike: HikeLog? = null,
    val creator: User? = null,
    val reviews: List<TrailReview> = emptyList(),
    val comments: List<HikeComment> = emptyList(),
    val authors: Map<String, User> = emptyMap(),
    val likeCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val currentUserUid: String? = null,
    val isMyHike: Boolean = false,
    val myReviewExists: Boolean = false,
)

/**
 * Decoupled state design:
 *  - The core [uiState] flow combines only the *required* pieces (hike,
 *    reviews, comments, likes, current uid). It emits as soon as Room
 *    answers — usually <50 ms — so the screen never gets stuck on
 *    "Loading hike…".
 *  - The creator user + the comment/review authors are looked up in a
 *    *separate* side-channel flow that we merge in only when it has data.
 *    A slow / missing creator profile never blocks the hike itself.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HikeDetailViewModel(
    private val hikeId: String,
    private val hikeLogRepository: HikeLogRepository,
    private val socialRepository: SocialRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val predictRepository: PredictRepository,
    private val getReviewSummaryUseCase: GetReviewSummaryUseCase,
) : ViewModel() {

    // ---------------- Prediction state ------------------------------------

    /**
     * Holds the current state of the AI prediction (Idle / Loading / Success / Error).
     *
     * It is a separate StateFlow from [uiState] so that updating the prediction
     * does not retrigger the heavy combine() chain that drives the rest of the screen.
     * The composable collects both flows independently.
     */
    private val _predictionState = MutableStateFlow<PredictionState>(PredictionState.Idle)
    val predictionState: StateFlow<PredictionState> = _predictionState.asStateFlow()

    /**
     * Called when the user taps "Predict My Time".
     *
     * What happens step by step:
     *  1. We immediately set the state to Loading so the UI shows a spinner.
     *  2. We fetch the signed-in user's full profile from Room (fast, already cached).
     *  3. We call [PredictRepository.predict] which sends the HTTP request to Python Anywhere.
     *  4. On success → state becomes Success(minutes).
     *     On any failure → state becomes Error with a human-readable message.
     *
     * The whole thing runs inside [viewModelScope], which means:
     *  - It runs on a background thread (never blocks the UI).
     *  - It is automatically cancelled if the user navigates away mid-request.
     */
    fun requestPrediction() {
        val hike = uiState.value.hike ?: return
        val uid  = uiState.value.currentUserUid ?: return

        viewModelScope.launch {
            _predictionState.value = PredictionState.Loading

            // Fetch the current user's profile. Room emits immediately from cache.
            val user = userRepository.observeUser(uid).first()
            if (user == null) {
                _predictionState.value =
                    PredictionState.Error("Could not load your profile. Please try again.")
                return@launch
            }

            predictRepository.predict(user = user, hike = hike).fold(
                onSuccess = { minutes ->
                    _predictionState.value = PredictionState.Success(minutes)
                },
                onFailure = {
                    _predictionState.value =
                        PredictionState.Error("Prediction failed. Check your connection and try again.")
                },
            )
        }
    }

    // ---------------- AI review summary -----------------------------------

    private val _summaryState = MutableStateFlow<ReviewSummaryState>(ReviewSummaryState.Idle)
    val summaryState: StateFlow<ReviewSummaryState> = _summaryState.asStateFlow()

    /**
     * Triggered only when the user taps "AI summary". Collects the written
     * review texts currently on screen and offloads summarization to the
     * backend LLM. A separate StateFlow from [uiState] so it doesn't retrigger
     * the heavy combine() chain.
     */
    fun summarizeReviews() {
        if (_summaryState.value is ReviewSummaryState.Loading) return
        val reviewTexts = uiState.value.reviews
            .mapNotNull { it.commentText?.takeIf { t -> t.isNotBlank() } }

        _summaryState.value = ReviewSummaryState.Loading
        viewModelScope.launch {
            getReviewSummaryUseCase(reviewTexts).fold(
                onSuccess = { summary ->
                    _summaryState.value = ReviewSummaryState.Success(summary)
                },
                onFailure = { t ->
                    val message = if (t is GetReviewSummaryUseCase.NoReviewsException) {
                        "No written reviews to summarize yet."
                    } else {
                        "Couldn't generate a summary. Check your connection and try again."
                    }
                    _summaryState.value = ReviewSummaryState.Error(message)
                },
            )
        }
    }

    // ---------------- Side-channel: cached author lookups ----------------

    /** Mutable cache of every user we've ever displayed in this screen. */
    private val authorsCache = MutableStateFlow<Map<String, User>>(emptyMap())

    /** Track which UIDs we've already kicked off a Firestore lookup for, so
     *  re-emissions of reviews/comments don't spam observeUser. */
    private val subscribedUids = mutableSetOf<String>()

    private fun ensureUserSubscribed(uid: String) {
        if (!subscribedUids.add(uid)) return
        userRepository.observeUser(uid)
            .onEach { user ->
                if (user != null) {
                    authorsCache.value = authorsCache.value + (uid to user)
                }
            }
            .launchIn(viewModelScope)
    }

    // ---------------- Core uiState -------------------------------------

    private val currentUidFlow = authRepository.authState.map {
        (it as? AuthState.SignedIn)?.user?.firebaseUid
    }

    private val hikeFlow = hikeLogRepository.observeHike(hikeId)

    private val reviewsFlow = socialRepository.observeReviewsForHike(hikeId)
        .onEach { rs -> rs.forEach { ensureUserSubscribed(it.reviewerUid) } }
    private val commentsFlow = socialRepository.observeComments(hikeId)
        .onEach { cs -> cs.forEach { ensureUserSubscribed(it.authorUid) } }

    /** Pair of (isLikedByMe, likeCount). When signed out, (false, 0). */
    private val likesFlow = currentUidFlow.flatMapLatest { uid ->
        if (uid == null) {
            flowOf(false to 0)
        } else {
            combine(
                hikeLogRepository.observeIsLiked(uid, hikeId),
                hikeLogRepository.observeLikeCount(hikeId),
            ) { liked, count -> liked to count }
        }
    }

    val uiState: StateFlow<HikeDetailUiState> = combine(
        hikeFlow,
        reviewsFlow,
        commentsFlow,
        likesFlow,
        currentUidFlow,
    ) { hike, reviews, comments, likesPair, uid ->
        val (liked, count) = likesPair
        // Subscribe to the creator's user document (best-effort) so we can
        // also offer their profile from the detail screen even if they're
        // not someone the current user has met before.
        if (hike != null) ensureUserSubscribed(hike.creatorFirebaseUid)
        HikeDetailUiState(
            loading = false,
            hike = hike,
            creator = null,
            reviews = reviews,
            comments = comments,
            authors = emptyMap(),
            likeCount = count,
            isLikedByMe = liked,
            currentUserUid = uid,
            isMyHike = hike != null && uid != null && hike.creatorFirebaseUid == uid,
            myReviewExists = uid != null && reviews.any { it.reviewerUid == uid },
        )
    }
        .combine(authorsCache) { state, authors ->
            state.copy(
                authors = authors,
                creator = state.hike?.creatorFirebaseUid?.let(authors::get),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HikeDetailUiState(loading = true),
        )

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

    suspend fun refresh() {
        runCatching { hikeLogRepository.refresh() }
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
                    predictRepository = app.container.predictRepository,
                    getReviewSummaryUseCase = GetReviewSummaryUseCase(
                        app.container.reviewSummaryRepository,
                    ),
                )
            }
        }
    }
}
