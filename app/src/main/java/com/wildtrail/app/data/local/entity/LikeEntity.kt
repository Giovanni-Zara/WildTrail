package com.wildtrail.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.wildtrail.app.domain.model.Like

/** Compound primary key (userUid, hikeId) — one row per (user, hike) pair. */
@Entity(
    tableName = "likes",
    primaryKeys = ["userUid", "hikeId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["firebaseUid"],
            childColumns = ["userUid"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = HikeLogEntity::class,
            parentColumns = ["hikeId"],
            childColumns = ["hikeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("hikeId")],
)
data class LikeEntity(
    val userUid: String,
    val hikeId: String,
    val createdAt: Long,
)

fun LikeEntity.toDomain(): Like = Like(userUid, hikeId, createdAt)
fun Like.toEntity(): LikeEntity = LikeEntity(userUid, hikeId, createdAt)
