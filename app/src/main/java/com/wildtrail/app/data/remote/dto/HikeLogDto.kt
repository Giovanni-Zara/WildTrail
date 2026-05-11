package com.wildtrail.app.data.remote.dto

import com.wildtrail.app.domain.model.GeoPoint
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.SurfaceType

/**
 * Firestore document shape for the `hikes` collection.
 *
 * The route is a List<Map<String, Any>> because Firestore can't directly
 * serialise our [GeoPoint] data class (it doesn't reflect Kotlin's
 * @Serializable). Storing them as plain maps keeps the data fully queryable
 * in the Firebase console.
 */
data class HikeLogDto(
    var hikeId: String = "",
    var creatorFirebaseUid: String = "",
    var workoutId: String? = null,
    var title: String = "",
    var description: String? = null,
    var avgSpeedKmh: Double = 0.0,
    var stepCount: Int = 0,
    var caloriesBurned: Int = 0,
    var coverPhotoUrl: String? = null,
    var xpEarned: Int = 0,
    var likesCount: Int = 0,
    /** Stored as the SurfaceType enum's `name`, e.g. "MOUNTAIN". */
    var surfaceType: String = SurfaceType.OTHER.name,
    var lengthKm: Double = 0.0,
    var durationSeconds: Long = 0L,
    var startedAt: Long = 0L,
    var endedAt: Long = 0L,
    var elevationGainMeters: Int = 0,
    var routeCoordinates: List<Map<String, Any?>> = emptyList(),
    var isPrivate: Boolean = false,
    var difficultyLevel: Int = 3,
    var mudRisk: Int = 3,
    var pathClarity: Int = 3,
    var fatigueLevel: Int = 3,
    var animalEncounterRisk: Int = 3,
    var waterAvailability: Boolean = false,
    var averageRating: Double = 0.0,
    var reviewCount: Int = 0,
)

private fun SurfaceType.code(): String = name
private fun String.toSurfaceType(): SurfaceType =
    runCatching { SurfaceType.valueOf(this) }.getOrDefault(SurfaceType.OTHER)

fun HikeLogDto.toDomain(): HikeLog = HikeLog(
    hikeId = hikeId,
    creatorFirebaseUid = creatorFirebaseUid,
    workoutId = workoutId,
    title = title,
    description = description,
    avgSpeedKmh = avgSpeedKmh.toFloat(),
    stepCount = stepCount,
    caloriesBurned = caloriesBurned,
    coverPhotoUrl = coverPhotoUrl,
    xpEarned = xpEarned,
    likesCount = likesCount,
    surfaceType = surfaceType.toSurfaceType(),
    lengthKm = lengthKm.toFloat(),
    durationSeconds = durationSeconds,
    startedAt = startedAt,
    endedAt = endedAt,
    elevationGainMeters = elevationGainMeters,
    routeCoordinates = routeCoordinates.map { m ->
        GeoPoint(
            lat = (m["lat"] as? Number)?.toDouble() ?: 0.0,
            lng = (m["lng"] as? Number)?.toDouble() ?: 0.0,
            altitudeM = (m["altitudeM"] as? Number)?.toDouble(),
            timestamp = (m["timestamp"] as? Number)?.toLong() ?: 0L,
        )
    },
    isPrivate = isPrivate,
    difficultyLevel = difficultyLevel,
    mudRisk = mudRisk,
    pathClarity = pathClarity,
    fatigueLevel = fatigueLevel,
    animalEncounterRisk = animalEncounterRisk,
    waterAvailability = waterAvailability,
    averageRating = averageRating.toFloat(),
    reviewCount = reviewCount,
)

fun HikeLog.toDto(): HikeLogDto = HikeLogDto(
    hikeId = hikeId,
    creatorFirebaseUid = creatorFirebaseUid,
    workoutId = workoutId,
    title = title,
    description = description,
    avgSpeedKmh = avgSpeedKmh.toDouble(),
    stepCount = stepCount,
    caloriesBurned = caloriesBurned,
    coverPhotoUrl = coverPhotoUrl,
    xpEarned = xpEarned,
    likesCount = likesCount,
    surfaceType = surfaceType.code(),
    lengthKm = lengthKm.toDouble(),
    durationSeconds = durationSeconds,
    startedAt = startedAt,
    endedAt = endedAt,
    elevationGainMeters = elevationGainMeters,
    routeCoordinates = routeCoordinates.map { gp ->
        mapOf(
            "lat" to gp.lat,
            "lng" to gp.lng,
            "altitudeM" to gp.altitudeM,
            "timestamp" to gp.timestamp,
        )
    },
    isPrivate = isPrivate,
    difficultyLevel = difficultyLevel,
    mudRisk = mudRisk,
    pathClarity = pathClarity,
    fatigueLevel = fatigueLevel,
    animalEncounterRisk = animalEncounterRisk,
    waterAvailability = waterAvailability,
    averageRating = averageRating.toDouble(),
    reviewCount = reviewCount,
)
