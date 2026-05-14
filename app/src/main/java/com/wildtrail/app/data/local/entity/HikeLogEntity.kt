package com.wildtrail.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wildtrail.app.domain.model.GeoPoint
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.SurfaceType

/**
 * A recorded hike. We keep the route as a JSON-encoded list of [GeoPoint]
 * (decoded by [com.wildtrail.app.data.local.converter.Converters]).
 *
 * **No foreign-key constraint to UserEntity**: the local DB caches public
 * hikes from *other* users too, and those users may not have a row in our
 * `users` table on this device. The creator's display info is denormalised
 * onto the hike itself (creatorUsername, creatorProfilePictureUrl), so we
 * can still render the feed.
 */
@Entity(
    tableName = "hike_logs",
    indices = [Index("creatorFirebaseUid"), Index("isPrivate")],
)
data class HikeLogEntity(
    @PrimaryKey val hikeId: String,
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

fun HikeLogEntity.toDomain(): HikeLog = HikeLog(
    hikeId = hikeId,
    creatorFirebaseUid = creatorFirebaseUid,
    creatorUsername = creatorUsername,
    creatorProfilePictureUrl = creatorProfilePictureUrl,
    workoutId = workoutId,
    title = title,
    description = description,
    avgSpeedKmh = avgSpeedKmh,
    stepCount = stepCount,
    caloriesBurned = caloriesBurned,
    coverPhotoUrl = coverPhotoUrl,
    xpEarned = xpEarned,
    likesCount = likesCount,
    surfaceType = surfaceType,
    lengthKm = lengthKm,
    durationSeconds = durationSeconds,
    startedAt = startedAt,
    endedAt = endedAt,
    elevationGainMeters = elevationGainMeters,
    routeCoordinates = routeCoordinates,
    isPrivate = isPrivate,
    difficultyLevel = difficultyLevel,
    mudRisk = mudRisk,
    pathClarity = pathClarity,
    fatigueLevel = fatigueLevel,
    animalEncounterRisk = animalEncounterRisk,
    waterAvailability = waterAvailability,
    averageRating = averageRating,
    reviewCount = reviewCount,
)

fun HikeLog.toEntity(): HikeLogEntity = HikeLogEntity(
    hikeId = hikeId,
    creatorFirebaseUid = creatorFirebaseUid,
    creatorUsername = creatorUsername,
    creatorProfilePictureUrl = creatorProfilePictureUrl,
    workoutId = workoutId,
    title = title,
    description = description,
    avgSpeedKmh = avgSpeedKmh,
    stepCount = stepCount,
    caloriesBurned = caloriesBurned,
    coverPhotoUrl = coverPhotoUrl,
    xpEarned = xpEarned,
    likesCount = likesCount,
    surfaceType = surfaceType,
    lengthKm = lengthKm,
    durationSeconds = durationSeconds,
    startedAt = startedAt,
    endedAt = endedAt,
    elevationGainMeters = elevationGainMeters,
    routeCoordinates = routeCoordinates,
    isPrivate = isPrivate,
    difficultyLevel = difficultyLevel,
    mudRisk = mudRisk,
    pathClarity = pathClarity,
    fatigueLevel = fatigueLevel,
    animalEncounterRisk = animalEncounterRisk,
    waterAvailability = waterAvailability,
    averageRating = averageRating,
    reviewCount = reviewCount,
)
