package com.wildtrail.app.domain.model

/**
 * A user's review of a specific public hike. We enforce one-review-per-user-
 * per-hike at the DAO level (UNIQUE constraint on (userId, hikeId)).
 *
 * [overallRating] is the headline 1..5 stars value used to compute the
 * hike's average score. The other fields capture trail-condition details.
 */
data class TrailReview(
    val reviewId: String,
    val reviewerUid: String,
    val hikeId: String,
    /** 1..5 stars — used for averaging into the hike score. */
    val overallRating: Int,
    val fatigueLevel: Int,
    val pathClarity: Int,
    val difficultyLevel: Int,
    val mudRisk: Int,
    val animalEncounterRisk: Int,
    val waterAvailability: Boolean,
    /** Optional free-text feedback the reviewer typed in. */
    val commentText: String? = null,
    /** Photos attached to the review. While a submission is still uploading
     *  these are device-local `file://` paths; once Storage upload completes
     *  they're swapped for cross-device HTTPS download URLs. */
    val imageUrls: List<String> = emptyList(),
    /** System time of submission — doubles as the review's timestamp. */
    val createdAt: Long,
)
