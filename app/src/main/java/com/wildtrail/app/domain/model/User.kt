package com.wildtrail.app.domain.model

/**
 * A user of the app, as seen by the UI / domain layer.
 *
 * This is *the* representation we want our ViewModels and Composables to work
 * with. Both the Room entity and the Firestore DTO can map to/from it; that
 * way the UI never has to know whether the data came from the cloud or the
 * local cache. (Repository pattern + clean architecture.)
 */
data class User(
    val firebaseUid: String,
    val username: String,
    /** Mandatory at signup; nullable here so cached / legacy rows don't break. */
    val sex: Sex?,
    /** Date of birth as epoch millis. We derive `age` from it on the fly. */
    val dateOfBirth: Long?,
    val country: String?,
    val level: Int,
    val xpPoints: Int,
    val totalDistanceKm: Float,
    val totalHikesCount: Int,
    val profilePictureUrl: String?,
    val bio: String?,
    val createdAt: Long,
    val lastActive: Long,
    val isPublic: Boolean,
)

enum class Sex { MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY }
