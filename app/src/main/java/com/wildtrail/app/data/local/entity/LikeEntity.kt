package com.wildtrail.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import com.wildtrail.app.domain.model.Like

/**
 * Compound primary key (userUid, hikeId) — one row per (user, hike) pair.
 *
 * No FK constraints — cross-device sync may reference users we don't have
 * locally (e.g. another user liked our hike).
 */
@Entity(
    tableName = "likes",
    primaryKeys = ["userUid", "hikeId"],
    indices = [Index("hikeId")],
)
data class LikeEntity(
    val userUid: String,
    val hikeId: String,
    val createdAt: Long,
)

fun LikeEntity.toDomain(): Like = Like(userUid, hikeId, createdAt)
fun Like.toEntity(): LikeEntity = LikeEntity(userUid, hikeId, createdAt)
