package com.wildtrail.app.domain.model

/**
 * A user's review of a specific public hike. We enforce one-review-per-user-
 * per-hike at the DAO level (UNIQUE constraint on (userId, hikeId)).
 */
data class TrailReview(
    val reviewId: String,
    val reviewerUid: String,
    val hikeId: String,
    /** Self-reported fatigue, 1..5. */
    val fatigueLevel: Int,
    /** How obvious the trail was to follow, 1..5. */
    val pathClarity: Int,
    /** Difficulty 1..5. */
    val difficultyLevel: Int,
    /** Likelihood of mud, 1..5. */
    val mudRisk: Int,
    /** Likelihood of wild-animal encounters, 1..5. */
    val animalEncounterRisk: Int,
    /** Are there streams / fountains along the way? */
    val waterAvailability: Boolean,
    val createdAt: Long,
)
