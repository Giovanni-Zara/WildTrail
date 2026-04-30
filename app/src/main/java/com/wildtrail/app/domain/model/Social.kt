package com.wildtrail.app.domain.model

/**
 * One row per (follower -> followee) relationship.
 * UNIQUE on (followerUid, followeeUid) at the DAO level.
 */
data class UserFollow(
    val followerUid: String,
    val followeeUid: String,
    val notifyOnNewHike: Boolean,
    val createdAt: Long,
)

/**
 * "I'm following this trail" — used so users can subscribe to a hike and
 * get notified when reviews are added.
 */
data class FollowedTrail(
    val userUid: String,
    val hikeId: String,
    val notifyOnNewReview: Boolean,
    val createdAt: Long,
)

/** A comment on a hike. Photos URLs are stored as a list. */
data class HikeComment(
    val commentId: String,
    val authorUid: String,
    val hikeId: String,
    val text: String,
    val photoUrls: List<String>,
    val createdAt: Long,
)
