package com.wildtrail.app.data.repository

import android.util.Log
import com.wildtrail.app.data.local.dao.UserDao
import com.wildtrail.app.data.local.entity.toDomain
import com.wildtrail.app.data.local.entity.toEntity
import com.wildtrail.app.data.remote.FirestoreService
import com.wildtrail.app.data.remote.dto.toDomain
import com.wildtrail.app.data.remote.dto.toDto
import com.wildtrail.app.domain.model.User
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
 *   1. Push to Firestore (remote is the source of truth for user profiles).
 *   2. Mirror into Room so the UI updates immediately.
 *
 * Crash-safety: Firestore listener errors are logged and swallowed.
 */
class UserRepository(
    private val userDao: UserDao,
    private val firestore: FirestoreService,
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

    suspend fun updateUser(user: User) {
        userDao.upsert(user.toEntity())
        runCatching { firestore.upsertUser(user.toDto()) }
            .onFailure { Log.w(TAG, "Firestore profile update skipped", it) }
    }

    suspend fun search(query: String): List<User> =
        userDao.searchByUsername(query).map { it.toDomain() }

    private companion object { const val TAG = "UserRepository" }
}
