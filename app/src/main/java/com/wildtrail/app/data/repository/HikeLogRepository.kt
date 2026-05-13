package com.wildtrail.app.data.repository

import android.util.Log
import com.wildtrail.app.data.local.dao.HikeLogDao
import com.wildtrail.app.data.local.dao.LikeDao
import com.wildtrail.app.data.local.dao.TrailReviewDao
import com.wildtrail.app.data.local.entity.toDomain
import com.wildtrail.app.data.local.entity.toEntity
import com.wildtrail.app.data.remote.FirestoreService
import com.wildtrail.app.data.remote.dto.LikeDto
import com.wildtrail.app.data.remote.dto.toDomain
import com.wildtrail.app.data.remote.dto.toDto
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.Like
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
    private val likeDao: LikeDao,
    private val reviewDao: TrailReviewDao,
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

    /** Force a re-pull from Firestore. Used by pull-to-refresh — even though
     *  the snapshot listeners already keep us live, this gives the user a
     *  visible "I'm syncing" feedback path. */
    suspend fun refresh() {
        // The active snapshot listeners will refresh themselves; we just
        // sleep briefly so the indicator doesn't flicker off instantly.
        kotlinx.coroutines.delay(600L)
    }

    // --- Likes -----------------------------------------------------------

    /** Hot stream: how many likes does this hike have? */
    fun observeLikeCount(hikeId: String): Flow<Int> {
        firestore.observeLikesForHike(hikeId)
            .catch { Log.w(TAG, "Firestore likes listener errored", it) }
            .onEach { dtos ->
                // Replace local cache for this hike with what Firestore returned.
                // Simplest correct approach: insert each one (REPLACE on conflict).
                dtos.forEach { likeDao.insert(it.toDomain().toEntity()) }
            }
            .launchIn(externalScope)
        return likeDao.observeLikeCount(hikeId)
    }

    /** Hot stream: has [uid] liked this hike? */
    fun observeIsLiked(uid: String, hikeId: String): Flow<Boolean> =
        likeDao.observeIsLikedByUser(uid, hikeId)

    /** All hikeIds that [uid] has liked — used by feed screens for filled hearts. */
    fun observeMyLikedHikeIds(uid: String): Flow<Set<String>> =
        likeDao.observeMyLikedHikeIds(uid).map { it.toSet() }

    /** Toggle the user's like on/off. Mirror to Firestore best-effort. */
    suspend fun setLiked(uid: String, hikeId: String, liked: Boolean) {
        if (liked) {
            val like = Like(uid, hikeId, System.currentTimeMillis())
            likeDao.insert(like.toEntity())
            runCatching { firestore.like(like.toDto()) }
                .onFailure { Log.w(TAG, "Firestore like skipped", it) }
        } else {
            likeDao.delete(uid, hikeId)
            runCatching { firestore.unlike(uid, hikeId) }
                .onFailure { Log.w(TAG, "Firestore unlike skipped", it) }
        }
    }

    /**
     * Recompute the hike's averageRating + reviewCount from the local
     * reviews and persist the result. Called after a new review is
     * submitted so the cards / details refresh instantly.
     */
    suspend fun refreshAggregateRating(hikeId: String) {
        val avg = reviewDao.getAvgOverallRating(hikeId) ?: 0.0
        val count = reviewDao.getCount(hikeId)
        val current = hikeLogDao.getById(hikeId) ?: return
        val updated = current.copy(
            averageRating = avg.toFloat(),
            reviewCount = count,
        )
        hikeLogDao.upsert(updated)
        runCatching { firestore.upsertHike(updated.toDomain().toDto()) }
            .onFailure { Log.w(TAG, "Firestore aggregate rating sync skipped", it) }
    }

    private companion object { const val TAG = "HikeLogRepository" }
}
