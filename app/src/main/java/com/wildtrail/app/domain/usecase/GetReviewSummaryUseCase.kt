package com.wildtrail.app.domain.usecase

import com.wildtrail.app.data.repository.ReviewSummaryRepository

/**
 * Domain entry point for "summarize this trail's reviews".
 *
 * It keeps the ViewModel free of repository wiring and is the natural place to
 * put any review-corpus preparation (here: drop blank entries) so the policy
 * lives in one testable spot rather than in the UI layer.
 */
class GetReviewSummaryUseCase(
    private val reviewSummaryRepository: ReviewSummaryRepository,
) {

    /**
     * @param reviewTexts raw review comment strings (may contain blanks).
     * @return [Result.success] with the summary text, or [Result.failure].
     */
    suspend operator fun invoke(reviewTexts: List<String>): Result<String> {
        val corpus = reviewTexts.map { it.trim() }.filter { it.isNotEmpty() }
        if (corpus.isEmpty()) {
            return Result.failure(NoReviewsException())
        }
        return reviewSummaryRepository.summarize(corpus)
    }

    /** Raised when there is no written review text to summarize. */
    class NoReviewsException : Exception("No written reviews to summarize yet")
}
