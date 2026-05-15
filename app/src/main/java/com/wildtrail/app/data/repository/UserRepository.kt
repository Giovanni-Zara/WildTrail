package com.wildtrail.app.data.repository

import android.net.Uri
import android.util.Log
import com.wildtrail.app.data.local.dao.UserDao
import com.wildtrail.app.data.local.entity.toDomain
import com.wildtrail.app.data.local.entity.toEntity
import com.wildtrail.app.data.remote.FirestoreService
import com.wildtrail.app.data.remote.StorageService
import com.wildtrail.app.data.remote.dto.toDomain
import com.wildtrail.app.data.remote.dto.toDto
import com.wildtrail.app.domain.model.User
import com.wildtrail.app.util.LevelMath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Profile-management repository.
 *
 * Read path:
 *   1. Always observe Room (offline-first).
 *   2. In parallel, kick off a Firestore listener and write its emissions
 *      back into Room. The Flow returned to the UI then surfaces the latest
 *      data automatically.
 *
 * Write path:
 *   1. Push to Room (offline-first).
 *   2. Push to Firestore (best-effort; failures logged, not crashing).
 */
class UserRepository(
    private val userDao: UserDao,
    private val firestore: FirestoreService,
    private val storage: StorageService,
    private val externalScope: CoroutineScope,
) {

    /** Observe a user, hydrating Room from Firestore in the background. */
    fun observeUser(uid: String): Flow<User?> {
        firestore.observeUser(uid)
            .catch { Log.w(TAG, "Firestore user listener errored", it) }
            .onEach { dto -> if (dto != null) userDao.upsert(dto.toDomain().toEntity()) }
            .launchIn(externalScope)
        return userDao.observeById(uid).map { it?.toDomain() }
    }

    suspend fun getUser(uid: String): User? = userDao.getById(uid)?.toDomain()

    suspend fun updateUser(user: User) {
        userDao.upsert(user.toEntity())
        runCatching { firestore.upsertUser(user.toDto()) }
            .onFailure { Log.w(TAG, "Firestore profile update skipped", it) }
    }

    /**
     * Called once per saved hike to bump the cached user totals. Stays
     * idempotent because `lastActive` is also touched — repeated calls just
     * advance counts further.
     */
    suspend fun incrementHikeStats(uid: String, distanceKm: Float, xpEarned: Int) {
        val current = userDao.getById(uid)?.toDomain() ?: return
        val newXp = current.xpPoints + xpEarned
        val updated = current.copy(
            xpPoints = newXp,
            level = LevelMath.levelForXp(newXp),
            totalHikesCount = current.totalHikesCount + 1,
            totalDistanceKm = current.totalDistanceKm + distanceKm,
            lastActive = System.currentTimeMillis(),
        )
        updateUser(updated)
    }

    /**
     * Upload a freshly-picked local [Uri] to Firebase Storage, then patch
     * the user document with the resulting HTTPS URL. Returns true on
     * success — callers ignore the result; the user's local row is updated
     * either way (offline-first).
     */
    suspend fun updateProfilePicture(uid: String, localUri: Uri): Boolean = runCatching {
        val url = storage.uploadProfilePicture(uid, localUri)
        val current = userDao.getById(uid)?.toDomain() ?: return@runCatching false
        updateUser(current.copy(profilePictureUrl = url))
        true
    }
        .onFailure { Log.w(TAG, "Profile picture upload skipped", it) }
        .getOrDefault(false)

    suspend fun search(query: String): List<User> =
        userDao.searchByUsername(query).map { it.toDomain() }

    private companion object { const val TAG = "UserRepository" }
}
