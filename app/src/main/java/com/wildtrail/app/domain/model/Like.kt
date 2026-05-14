package com.wildtrail.app.domain.model

/**
 * A "like" by a user on a hike. UNIQUE on (userUid, hikeId).
 *
 * Likes are stored as their own table/collection so we can compute counts
 * with simple SQL aggregates and Firestore queries, and so a user can toggle
 * their own like on/off without rewriting the whole hike doc.
 */
data class Like(
    val userUid: String,
    val hikeId: String,
    val createdAt: Long,
)
