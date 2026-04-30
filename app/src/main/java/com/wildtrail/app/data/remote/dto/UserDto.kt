package com.wildtrail.app.data.remote.dto

import com.wildtrail.app.domain.model.User

/**
 * Firestore document shape for the `users` collection.
 *
 * Firestore deserialises into POJOs via reflection — the class therefore needs:
 *  - a no-arg constructor (achieved by giving every field a default value)
 *  - public properties (Kotlin `var` works)
 *
 * We deliberately keep this DTO separate from [User] so changes to the cloud
 * shape don't ripple through the whole UI.
 */
data class UserDto(
    var firebaseUid: String = "",
    var username: String = "",
    var age: Int? = null,
    var country: String? = null,
    var level: Int = 1,
    var xpPoints: Int = 0,
    var totalDistanceKm: Double = 0.0,
    var totalHikesCount: Int = 0,
    var profilePictureUrl: String? = null,
    var bio: String? = null,
    var createdAt: Long = 0L,
    var lastActive: Long = 0L,
    var isPublic: Boolean = true,
)

fun UserDto.toDomain(): User = User(
    firebaseUid = firebaseUid,
    username = username,
    age = age,
    country = country,
    level = level,
    xpPoints = xpPoints,
    totalDistanceKm = totalDistanceKm.toFloat(),
    totalHikesCount = totalHikesCount,
    profilePictureUrl = profilePictureUrl,
    bio = bio,
    createdAt = createdAt,
    lastActive = lastActive,
    isPublic = isPublic,
)

fun User.toDto(): UserDto = UserDto(
    firebaseUid = firebaseUid,
    username = username,
    age = age,
    country = country,
    level = level,
    xpPoints = xpPoints,
    totalDistanceKm = totalDistanceKm.toDouble(),
    totalHikesCount = totalHikesCount,
    profilePictureUrl = profilePictureUrl,
    bio = bio,
    createdAt = createdAt,
    lastActive = lastActive,
    isPublic = isPublic,
)
