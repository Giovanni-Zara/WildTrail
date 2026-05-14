package com.wildtrail.app.domain.model

import kotlinx.serialization.Serializable

/**
 * The "trekking session" model — a single recorded hike, public or private.
 *
 * The route itself is stored as a list of [GeoPoint]s; we keep them as a
 * @Serializable class so the same shape can be persisted as JSON in Room
 * (one TEXT column) and as an array of maps in Firestore.
 *
 * The "characteristics" fields ([difficultyLevel], [mudRisk], [pathClarity],
 * [fatigueLevel], [animalEncounterRisk], [waterAvailability]) are filled in
 * by the **creator** at save time. Other users then leave reviews
 * (see [TrailReview]) whose `overallRating` values are averaged into the
 * hike's [averageRating] / [reviewCount].
 *
 * [creatorUsername] / [creatorProfilePictureUrl] are a denormalised snapshot
 * of the creator at save time. We store them on the hike itself so the
 * Home/Explore feed can show "posted by ___" *without* needing the
 * creator's full user document on the local device — which solves the
 * cross-device social-feed case where device B has never met user A.
 */
data class HikeLog(
    val hikeId: String,
    val creatorFirebaseUid: String,
    val creatorUsername: String,
    val creatorProfilePictureUrl: String?,
    val workoutId: String?,
    val title: String,
    val description: String?,
    val avgSpeedKmh: Float,
    val stepCount: Int,
    val caloriesBurned: Int,
    val coverPhotoUrl: String?,
    val xpEarned: Int,
    val likesCount: Int,
    val surfaceType: SurfaceType,
    val lengthKm: Float,
    val durationSeconds: Long,
    val startedAt: Long,
    val endedAt: Long,
    val elevationGainMeters: Int,
    val routeCoordinates: List<GeoPoint>,
    val isPrivate: Boolean,
    val difficultyLevel: Int,
    val mudRisk: Int,
    val pathClarity: Int,
    val fatigueLevel: Int,
    val animalEncounterRisk: Int,
    val waterAvailability: Boolean,
    val averageRating: Float,
    val reviewCount: Int,
)

enum class SurfaceType {
    MOUNTAIN, FOREST, COASTAL, URBAN, DESERT, OTHER
}

/**
 * A single GPS sample from the route. We *intentionally* keep this small —
 * a long hike can produce thousands of points and we don't want to bloat
 * Firestore documents (which are capped at 1 MiB).
 */
@Serializable
data class GeoPoint(
    val lat: Double,
    val lng: Double,
    val altitudeM: Double? = null,
    /** Epoch millis — handy for replaying a hike at correct cadence. */
    val timestamp: Long = 0L,
)
