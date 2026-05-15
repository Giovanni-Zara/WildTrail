package com.wildtrail.app.util

import com.wildtrail.app.domain.model.AchievementCategory
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.User

/**
 * Computes the metric for a given achievement [AchievementCategory]
 * against a user's current stats. The metric is then compared against
 * `AchievementDefinition.thresholdValue` in
 * [com.wildtrail.app.data.repository.AchievementRepository.evaluateAndAward].
 *
 *  - **DISTANCE**: cumulative kilometres recorded (user.totalDistanceKm).
 *  - **ELEVATION**: the *best* (max) single-hike elevation gain in metres.
 *    Per-hike makes "Mountain Goat: 1000m in one hike" actually feel like
 *    an achievement — cumulative elevation would be trivial to grind.
 *  - **SOCIAL**: number of public hikes the user has shared.
 *  - **STREAK**: total number of hikes recorded.
 *  - **BIODIVERSITY / OTHER**: placeholders that return 0 — reserved for
 *    future features (nature-discovery sightings, etc.).
 */
object AchievementEngine {

    fun metricFor(
        category: AchievementCategory,
        user: User,
        hikes: List<HikeLog>,
    ): Float = when (category) {
        AchievementCategory.DISTANCE -> user.totalDistanceKm
        AchievementCategory.ELEVATION ->
            hikes.maxOfOrNull { it.elevationGainMeters }?.toFloat() ?: 0f
        AchievementCategory.SOCIAL ->
            hikes.count { !it.isPrivate }.toFloat()
        AchievementCategory.STREAK -> user.totalHikesCount.toFloat()
        AchievementCategory.BIODIVERSITY -> 0f
        AchievementCategory.OTHER -> 0f
    }
}
