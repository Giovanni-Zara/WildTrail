package com.wildtrail.app.domain.usecase

import android.net.Uri
import com.wildtrail.app.data.repository.HikeLogRepository
import com.wildtrail.app.data.repository.SocialRepository
import com.wildtrail.app.domain.model.TrailReview

/**
 * Persists a [TrailReview] and keeps the hike's aggregate rating in sync.
 *
 * Stateless and callable via the `operator fun invoke` convention, so call
 * sites read like `submitReviewUseCase(review, uris)`. The heavy lifting
 * (offline-first Room write, photo copy, Storage upload, Firestore push)
 * lives in [SocialRepository.submitReview]; this use case just sequences it
 * with the average-rating refresh so the detail screen's "Community rating"
 * block updates as soon as the review lands.
 */
class SubmitReviewUseCase(
    private val socialRepository: SocialRepository,
    private val hikeLogRepository: HikeLogRepository,
) {
    suspend operator fun invoke(
        review: TrailReview,
        localImageUris: List<Uri> = emptyList(),
    ): Result<Unit> = runCatching {
        socialRepository.submitReview(review, localImageUris)
        hikeLogRepository.refreshAggregateRating(review.hikeId)
    }
}
