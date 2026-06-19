package com.wildtrail.app.data.repository

import com.wildtrail.app.data.remote.HikeApiService
import com.wildtrail.app.data.remote.dto.SummarizeReviewsRequestDto

/**
 * Repository for the AI review-summary feature (computation offloading: the
 * LLM runs on the PythonAnywhere backend, never on-device).
 *
 * **Offline / failure strategy.** Unlike hike data (which we cache in Room and
 * serve offline-first), a review *summary* is intentionally **not cached**:
 *  - it is ephemeral — the user explicitly asks for it and it is disposed when
 *    they leave the screen, so a stale cached summary would be misleading;
 *  - it depends on a network LLM that simply can't run without connectivity.
 *
 * So "offline-first" here means **graceful degradation**: the call is wrapped
 * in [runCatching], so no-network / HTTP error / JSON parse failure all surface
 * as a [Result.failure] (never a crash). The ViewModel turns that into an
 * Error state and the UI keeps the "AI summary" button so the user can retry
 * once they're back online.
 */
class ReviewSummaryRepository(
    private val hikeApiService: HikeApiService,
) {

    /**
     * Send the review text corpus to the backend and return the generated
     * summary, or a [Result.failure] on any error.
     */
    suspend fun summarize(reviews: List<String>): Result<String> = runCatching {
        require(reviews.isNotEmpty()) { "No reviews to summarize" }
        hikeApiService
            .summarizeReviews(SummarizeReviewsRequestDto(reviews))
            .summary
    }
}
