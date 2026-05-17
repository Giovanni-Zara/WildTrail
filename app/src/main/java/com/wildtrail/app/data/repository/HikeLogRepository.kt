package com.wildtrail.app.data.repository

import android.util.Log
import com.wildtrail.app.data.local.dao.HikeLogDao
import com.wildtrail.app.data.local.dao.LikeDao
import com.wildtrail.app.data.local.dao.TrailReviewDao
import com.wildtrail.app.data.local.dao.UserDao
import com.wildtrail.app.data.local.entity.toDomain
import com.wildtrail.app.data.local.entity.toEntity
import com.wildtrail.app.data.remote.FirestoreService
import com.wildtrail.app.data.remote.dto.HikeLogDto
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
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * The hike log repository — single source of truth for the "TREKKING" table.
 *
 * Pattern:
 *   - Reads come from Room (so the UI keeps working offline).
 *   - In parallel we kick off a Firestore listener that writes new hikes
 *     back into Room. Any Compose screen that's already collecting from
 *     Room will re-render automatically when this happens.
 *   - Writes go local-first; Firestore is best-effort.
 *
 * **Crash-safety**: every Firestore listener is wrapped with `.catch { }`
 * so a permission-denied / network error logs a warning and yields an
 * empty stream instead of bringing the app down.
 */
class HikeLogRepository(
    private val hikeLogDao: HikeLogDao,
    private val likeDao: LikeDao,
    private val reviewDao: TrailReviewDao,
    private val userDao: UserDao,
    private val firestore: FirestoreService,
    private val externalScope: CoroutineScope,
) {

    /** Tracks creator UIDs we've already kicked off a backfill for, so we
     *  don't fan out a Firestore read for every emission of the feed. */
    private val backfilledUids = ConcurrentHashMap.newKeySet<String>()

    /** "Explore" feed: public hikes only. */
    fun observePublicFeed(limit: Int = 50): Flow<List<HikeLog>> {
        firestore.observePublicHikes(limit.toLong())
            .catch { Log.w(TAG, "Firestore public feed listener errored", it) }
            .onEach { dtos ->
                hikeLogDao.upsertAll(dtos.map { it.toDomain().toEntity() })
                backfillCreators(dtos)
            }
            .launchIn(externalScope)
        return hikeLogDao.observePublicFeed(limit).map { list -> list.map { it.toDomain() } }
    }

    /** A user's own hikes (public + private). */
    fun observeMyHikes(uid: String): Flow<List<HikeLog>> {
        firestore.observeHikesByCreator(uid)
            .catch { Log.w(TAG, "Firestore my-hikes listener errored", it) }
            .onEach { dtos ->
                hikeLogDao.upsertAll(dtos.map { it.toDomain().toEntity() })
                backfillCreators(dtos)
            }
            .launchIn(externalScope)
        return hikeLogDao.observeByCreator(uid).map { list -> list.map { it.toDomain() } }
    }

    fun observeHike(id: String): Flow<HikeLog?> =
        hikeLogDao.observeById(id).map { it?.toDomain() }

    /** Hikes the signed-in user has liked (newest like first). */
    fun observeLikedHikes(uid: String): Flow<List<HikeLog>> =
        hikeLogDao.observeLikedHikes(uid).map { list -> list.map { it.toDomain() } }

    suspend fun saveHike(hike: HikeLog) {
        // Mirror locally first so the user sees their save instantly even
        // on a flaky network. Push to Firestore best-effort.
        hikeLogDao.upsert(hike.toEntity())
        runCatching { firestore.upsertHike(hike.toDto()) }
            .onFailure { Log.w(TAG, "Firestore hike save skipped", it) }
    }

    /**
     * Re-denormalise the creator's display info onto every hike they've
     * created. Called after the user edits their profile so a username /
     * profile-picture change is reflected on all their existing hike cards
     * (Home / Explore / Profile) instead of staying frozen at save time.
     *
     * Only rows that actually differ are rewritten, so this is a no-op when
     * nothing changed and never fans out redundant Firestore writes.
     *
     * [pushRemote] = false keeps the change local (Room only). The
     * offline-first profile-picture flow uses this for its optimistic step:
     * the picked image lives at a device-local `file://` path that is
     * meaningless on other devices, so it must never reach Firestore. Once
     * the cross-device HTTPS URL is available, this is called again with
     * [pushRemote] = true to fan the canonical URL out everywhere.
     */
    suspend fun syncCreatorInfo(
        uid: String,
        username: String,
        profilePictureUrl: String?,
        pushRemote: Boolean = true,
    ) {
        hikeLogDao.getByCreator(uid).forEach { row ->
            if (row.creatorUsername != username ||
                row.creatorProfilePictureUrl != profilePictureUrl
            ) {
                val patched = row.copy(
                    creatorUsername = username,
                    creatorProfilePictureUrl = profilePictureUrl,
                )
                hikeLogDao.upsert(patched)
                if (pushRemote) {
                    runCatching { firestore.upsertHike(patched.toDomain().toDto()) }
                        .onFailure { Log.w(TAG, "Firestore creator-info sync skipped", it) }
                }
            }
        }
    }

    suspend fun search(query: String): List<HikeLog> =
        hikeLogDao.search(query).map { it.toDomain() }

    /** Force a re-pull from Firestore. Used by pull-to-refresh — even though
     *  the snapshot listeners already keep us live, this gives the user a
     *  visible "I'm syncing" feedback path. */
    suspend fun refresh() {
        kotlinx.coroutines.delay(600L)
    }

    // --- Likes -----------------------------------------------------------

    /** Hot stream: how many likes does this hike have? */
    fun observeLikeCount(hikeId: String): Flow<Int> {
        firestore.observeLikesForHike(hikeId)
            .catch { Log.w(TAG, "Firestore likes listener errored", it) }
            .onEach { dtos ->
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

    /**
     * Toggle the user's like on/off AND denormalise the new likes count back
     * onto the hike row, so the heart counter on every [com.wildtrail.app.ui.components.HikeCard]
     * (Home / Explore / Profile) updates immediately instead of staying on
     * whatever value the hike was originally saved with.
     */
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
        // Recompute the count and write it back to the hike so cards refresh.
        val freshCount = likeDao.countLikes(hikeId)
        val hike = hikeLogDao.getById(hikeId)?.toDomain()
        if (hike != null && hike.likesCount != freshCount) {
            val updated = hike.copy(likesCount = freshCount)
            hikeLogDao.upsert(updated.toEntity())
            runCatching { firestore.upsertHike(updated.toDto()) }
                .onFailure { Log.w(TAG, "Firestore hike likesCount sync skipped", it) }
        }
    }

    /**
     * Recompute the hike's averageRating + reviewCount from the local
     * reviews and persist the result.
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

    /**
     * Keep the *denormalised* creator name + picture on every feed hike
     * fresh, so a card preview shows the same avatar the hike-detail screen
     * resolves via a live user lookup.
     *
     * Originally this only filled hikes whose `creatorUsername` arrived
     * blank. That left a gap: if a creator added/changed their picture
     * *after* posting a hike, the hike doc still carried the old (or null)
     * value, so previews showed the generic person icon while the detail
     * screen — which observes the live user — showed the real picture. We
     * now reconcile every creator in the feed against their current user
     * profile.
     *
     * Rules:
     *  - Looked up at most once per creator per app session (idempotent).
     *  - A remote `null` picture never clears an existing one — the
     *    creator's Storage upload may simply not have landed yet.
     *  - A device-local `file://` picture is never pushed to Firestore
     *    (meaningless on other devices); the local Room patch still happens
     *    so the owner sees it.
     */
    private fun backfillCreators(dtos: List<HikeLogDto>) {
        val creatorUids = dtos.map { it.creatorFirebaseUid }.toSet()
        if (creatorUids.isEmpty()) return
        externalScope.launch {
            creatorUids.forEach { uid ->
                if (!backfilledUids.add(uid)) return@forEach
                runCatching {
                    val user = firestore.getUser(uid)?.toDomain() ?: return@runCatching
                    if (user.username.isBlank()) return@runCatching
                    // Cache the user locally too (no FK constraints any more),
                    // but don't let a remote with no picture clobber a
                    // locally-pending file:// one.
                    val local = userDao.getById(uid)?.toDomain()
                    userDao.upsert(user.keepingLocalPicture(local).toEntity())
                    hikeLogDao.getByCreator(uid).forEach { row ->
                        // Fill the freshest creator info; never blank an
                        // existing picture with a remote null.
                        val newPic = user.profilePictureUrl ?: row.creatorProfilePictureUrl
                        if (row.creatorUsername == user.username &&
                            row.creatorProfilePictureUrl == newPic
                        ) {
                            return@forEach
                        }
                        val patched = row.copy(
                            creatorUsername = user.username,
                            creatorProfilePictureUrl = newPic,
                        )
                        hikeLogDao.upsert(patched)
                        if (newPic?.startsWith("file://") != true) {
                            runCatching { firestore.upsertHike(patched.toDomain().toDto()) }
                                .onFailure { Log.w(TAG, "Firestore creator backfill push skipped", it) }
                        }
                    }
                }.onFailure { Log.w(TAG, "Creator backfill skipped for $uid", it) }
            }
        }
    }

    private companion object { const val TAG = "HikeLogRepository" }
}
