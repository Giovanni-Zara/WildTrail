package com.wildtrail.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wildtrail.app.domain.model.GeoPoint
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.SurfaceType

/**
 * A recorded hike. We keep the route as a JSON-encoded list of [GeoPoint]
 * (decoded by [com.wildtrail.app.data.local.converter.Converters]).
 *
 * Foreign key to [UserEntity] uses ON DELETE CASCADE: deleting an account
 * locally removes the hikes too.
 */
@Entity(
    tableName = "hike_logs",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["firebaseUid"],
            childColumns = ["creatorFirebaseUid"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("creatorFirebaseUid"), Index("isPrivate")],
)
data class HikeLogEntity(
    @PrimaryKey val hikeId: String,
    val creatorFirebaseUid: String,
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
)

fun HikeLogEntity.toDomain(): HikeLog = HikeLog(
    hikeId = hikeId,
    creatorFirebaseUid = creatorFirebaseUid,
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
)

fun HikeLog.toEntity(): HikeLogEntity = HikeLogEntity(
    hikeId = hikeId,
    creatorFirebaseUid = creatorFirebaseUid,
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
)
