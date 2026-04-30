package com.wildtrail.app.data.repository

import android.util.Log
import com.wildtrail.app.data.local.dao.HikeLogDao
import com.wildtrail.app.data.local.entity.toDomain
import com.wildtrail.app.data.local.entity.toEntity
import com.wildtrail.app.data.remote.FirestoreService
import com.wildtrail.app.data.remote.dto.toDomain
import com.wildtrail.app.data.remote.dto.toDto
import com.wildtrail.app.domain.model.HikeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * The hike log repository — single source of truth for the "TREKKING" table.
 *
 * Pattern:
 *   - Reads come from Room (so the UI keeps working offline).
 *   - In parallel we kick off a Firestore listener that writes new hikes
 *     back into Room. Any Compose screen that's already collecting from
 *     Room will re-render automatically when this happens.
 *   - Writes go remote-first — but we *also* mirror them locally so the
 *     UI is updated instantly even before Firestore has acknowledged.
 *
 * **Crash-safety**: every Firestore listener is wrapped with `.catch { }`
 * so a permission-denied / network error logs a warning and yields an
 * empty stream instead of bringing the app down.
 */
class HikeLogRepository(
    private val hikeLogDao: HikeLogDao,
    private val firestore: FirestoreService,
    private val externalScope: CoroutineScope,
) {

    /** "Explore" feed: public hikes only. */
    fun observePublicFeed(limit: Int = 50): Flow<List<HikeLog>> {
        firestore.observePublicHikes(limit.toLong())
            .catch { Log.w(TAG, "Firestore public feed listener errored", it) }
            .onEach { dtos -> hikeLogDao.upsertAll(dtos.map { it.toDomain().toEntity() }) }
            .launchIn(externalScope)
        return hikeLogDao.observePublicFeed(limit).map { list -> list.map { it.toDomain() } }
    }

    /** A user's own hikes (public + private). */
    fun observeMyHikes(uid: String): Flow<List<HikeLog>> {
        firestore.observeHikesByCreator(uid)
            .catch { Log.w(TAG, "Firestore my-hikes listener errored", it) }
            .onEach { dtos -> hikeLogDao.upsertAll(dtos.map { it.toDomain().toEntity() }) }
            .launchIn(externalScope)
        return hikeLogDao.observeByCreator(uid).map { list -> list.map { it.toDomain() } }
    }

    fun observeHike(id: String): Flow<HikeLog?> =
        hikeLogDao.observeById(id).map { it?.toDomain() }

    suspend fun saveHike(hike: HikeLog) {
        // Mirror locally first so the user sees their save instantly even
        // on a flaky network. Push to Firestore best-effort.
        hikeLogDao.upsert(hike.toEntity())
        runCatching { firestore.upsertHike(hike.toDto()) }
            .onFailure { Log.w(TAG, "Firestore hike save skipped", it) }
    }

    suspend fun search(query: String): List<HikeLog> =
        hikeLogDao.search(query).map { it.toDomain() }

    private companion object { const val TAG = "HikeLogRepository" }
}
