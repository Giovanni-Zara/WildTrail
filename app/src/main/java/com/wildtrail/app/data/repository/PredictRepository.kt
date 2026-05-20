package com.wildtrail.app.data.repository

import com.wildtrail.app.data.remote.HikeApiService
import com.wildtrail.app.data.remote.dto.HikeFeaturesDto
import com.wildtrail.app.data.remote.dto.PredictRequestDto
import com.wildtrail.app.data.remote.dto.UserFeaturesDto
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.SurfaceType
import com.wildtrail.app.domain.model.User

/**
 * Handles the ML prediction call.
 *
 * Why a Repository?
 *   The ViewModel must not know *how* predictions are fetched — whether from the
 *   network, a local cache, or a stub. The Repository hides that detail. If we
 *   later add caching or switch backends, only this file changes.
 *
 * [predict] wraps the network call in [runCatching] so any exception
 * (no network, HTTP 4xx/5xx, JSON parse error) is captured and returned as
 * a [Result.failure] instead of crashing the app.
 */
class PredictRepository(
    private val hikeApiService: HikeApiService,
) {

    /**
     * Sends the user + hike features to the ML backend and returns the
     * predicted hiking duration in minutes.
     *
     * @return [Result.success] with the predicted minutes, or
     *         [Result.failure] with the caught exception.
     */
    suspend fun predict(user: User, hike: HikeLog): Result<Double> = runCatching {
        val age = computeAge(user.dateOfBirth)
        val avgSpeed = computeAvgSpeed(user.xpPoints, age)

        val request = PredictRequestDto(
            user = UserFeaturesDto(
                xpPoints = user.xpPoints,
                eta = age,
                pastHikes = user.totalHikesCount,
                avgSpeed = avgSpeed,
            ),
            hike = HikeFeaturesDto(
                lunghezza = hike.lengthKm.toDouble(),
                elevationGain = hike.elevationGainMeters,
                surfaceType = hike.surfaceType.toApiString(),
                difficulty = hike.difficultyLevel,
            ),
        )

        hikeApiService.predict(request).durationMin
    }

    // --------------- private helpers ----------------------------------------

    /**
     * Derives age in years from a stored epoch-millis date of birth.
     * Falls back to 30 if the user hasn't set a birthday yet.
     */
    private fun computeAge(dateOfBirth: Long?): Int {
        dateOfBirth ?: return 30
        val ageMs = System.currentTimeMillis() - dateOfBirth
        return (ageMs / (1000L * 60 * 60 * 24 * 365)).toInt().coerceIn(10, 100)
    }

    /**
     * Estimates the user's average hiking speed using the same formula used
     * to generate the synthetic training dataset, so the feature is on the
     * same scale the model was trained on.
     *
     * Formula: 4.2 + 0.00018 * xpPoints − 0.015 * max(0, age − 45)
     * Clamped to the training range [2.0, 7.0] km/h.
     */
    private fun computeAvgSpeed(xpPoints: Int, age: Int): Double =
        (4.2 + 0.00018 * xpPoints - 0.015 * maxOf(0, age - 45)).coerceIn(2.0, 7.0)

    /**
     * Maps the app's [SurfaceType] enum to the string values the Python
     * model was trained on.
     */
    private fun SurfaceType.toApiString(): String = when (this) {
        SurfaceType.MOUNTAIN -> "mountain"
        SurfaceType.FOREST   -> "forest"
        SurfaceType.COASTAL  -> "mixed"
        SurfaceType.URBAN    -> "road"
        SurfaceType.DESERT   -> "mixed"
        SurfaceType.OTHER    -> "mixed"
    }
}
