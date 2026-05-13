package com.wildtrail.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wildtrail.app.domain.model.TrailReview

/**
 * A review of a hike. The compound UNIQUE index on (reviewerUid, hikeId)
 * enforces "one review per user per hike", matching the original schema
 * sketch.
 *
 * No FK constraints on the local table — see [HikeLogEntity] for rationale
 * (we cache other users' content even when we don't have those users in
 * our local `users` row).
 */
@Entity(
    tableName = "trail_reviews",
    indices = [
        Index(value = ["reviewerUid", "hikeId"], unique = true),
        Index("hikeId"),
    ],
)
data class TrailReviewEntity(
    @PrimaryKey val reviewId: String,
    val reviewerUid: String,
    val hikeId: String,
    val overallRating: Int,
    val fatigueLevel: Int,
    val pathClarity: Int,
    val difficultyLevel: Int,
    val mudRisk: Int,
    val animalEncounterRisk: Int,
    val waterAvailability: Boolean,
    val createdAt: Long,
)

fun TrailReviewEntity.toDomain(): TrailReview = TrailReview(
    reviewId = reviewId,
    reviewerUid = reviewerUid,
    hikeId = hikeId,
    overallRating = overallRating,
    fatigueLevel = fatigueLevel,
    pathClarity = pathClarity,
    difficultyLevel = difficultyLevel,
    mudRisk = mudRisk,
    animalEncounterRisk = animalEncounterRisk,
    waterAvailability = waterAvailability,
    createdAt = createdAt,
)

fun TrailReview.toEntity(): TrailReviewEntity = TrailReviewEntity(
    reviewId = reviewId,
    reviewerUid = reviewerUid,
    hikeId = hikeId,
    overallRating = overallRating,
    fatigueLevel = fatigueLevel,
    pathClarity = pathClarity,
    difficultyLevel = difficultyLevel,
    mudRisk = mudRisk,
    animalEncounterRisk = animalEncounterRisk,
    waterAvailability = waterAvailability,
    createdAt = createdAt,
)
