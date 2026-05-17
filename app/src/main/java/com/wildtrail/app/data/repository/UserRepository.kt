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
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.User
import com.wildtrail.app.util.LevelMath
import com.wildtrail.app.util.LocalImageStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    private val imageStore: LocalImageStore,
    private val hikeLogRepository: HikeLogRepository,
    private val externalScope: CoroutineScope,
) {

    /** Observe a user, hydrating Room from Firestore in the background. */
    fun observeUser(uid: String): Flow<User?> {
        firestore.observeUser(uid)
            .catch { Log.w(TAG, "Firestore user listener errored", it) }
            .onEach { dto ->
                if (dto != null) {
                    val remote = dto.toDomain()
                    val local = userDao.getById(remote.firebaseUid)?.toDomain()
                    userDao.upsert(remote.keepingLocalPicture(local).toEntity())
                }
            }
            .launchIn(externalScope)
        return userDao.observeById(uid).map { it?.toDomain() }
    }

    suspend fun getUser(uid: String): User? = userDao.getById(uid)?.toDomain()

    /** Side-effect-free Room observer (no Firestore listener). */
    private fun observeCachedUser(uid: String): Flow<User?> =
        userDao.observeById(uid).map { it?.toDomain() }

    /**
     * Enrich a feed of hikes with each creator's *live* profile picture, the
     * way the hike-detail screen already does for a single creator.
     *
     * The picture denormalised onto a [HikeLog] at save time goes stale the
     * moment the creator changes (or first sets) their picture, so list
     * previews would show the generic icon while the detail screen — which
     * observes the creator's user row — shows the real photo. Here we splice
     * the current Room picture onto every card so the preview matches.
     *
     * We read straight from the Room cache (no per-uid Firestore listeners
     * to leak): creator rows are already hydrated by sign-up /
     * [updateProfilePicture] / [observeUser] / the creator backfill, and the
     * common case — the signed-in user's own hikes — is always cached. If a
     * creator isn't cached yet we simply fall back to the denormalised value
     * (unchanged behaviour), and the card lights up once it is.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun withLiveCreatorPictures(hikes: Flow<List<HikeLog>>): Flow<List<HikeLog>> =
        // Collect `hikes` exactly once (the underlying feed flow starts a
        // Firestore listener as a side effect). observeCachedUser is a pure
        // Room flow, so re-subscribing the inner combine per emission is safe.
        hikes.flatMapLatest { list ->
            val ids = list.map { it.creatorFirebaseUid }.toSet()
            if (ids.isEmpty()) {
                flowOf(list)
            } else {
                combine(
                    ids.map { id -> observeCachedUser(id).map { id to it?.profilePictureUrl } },
                ) { pairs ->
                    val pics = pairs.toMap()
                    list.map { hike ->
                        val live = pics[hike.creatorFirebaseUid]
                        if (!live.isNullOrBlank()) {
                            hike.copy(creatorProfilePictureUrl = live)
                        } else {
                            hike
                        }
                    }
                }
            }
        }

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
     * Offline-first profile-picture update.
     *
     * 1. Copy the picked image into app-owned storage and write that path
     *    to **Room only**, so the new picture shows up immediately and
     *    survives with no network / Firebase Storage disabled. We must not
     *    push a device-local `file://` path to Firestore — it is meaningless
     *    on other devices and would corrupt the shared user document.
     * 2. Best-effort upload the local copy to Firebase Storage. On success,
     *    swap to the cross-device HTTPS URL (Room + Firestore via
     *    [updateUser]).
     *
     * Returns true if the remote upload succeeded; callers ignore it — the
     * local row is updated regardless.
     */
    suspend fun updateProfilePicture(uid: String, localUri: Uri): Boolean {
        val current = userDao.getById(uid)?.toDomain() ?: return false

        val localFile = runCatching { imageStore.saveProfilePicture(uid, localUri) }
            .onFailure { Log.w(TAG, "Local profile picture copy failed", it) }
            .getOrNull()
        if (localFile != null) {
            val localPath = Uri.fromFile(localFile).toString()
            userDao.upsert(current.copy(profilePictureUrl = localPath).toEntity())
            // Re-stamp the new picture onto the user's existing hike cards on
            // this device. Room only — a file:// path must not reach Firestore.
            hikeLogRepository.syncCreatorInfo(
                uid, current.username, localPath, pushRemote = false,
            )
        }

        // Upload the file we own rather than the transient content:// URI,
        // sidestepping the Photo Picker's temporary read-grant entirely.
        val uploadSource = localFile?.let(Uri::fromFile) ?: localUri
        return runCatching {
            val url = storage.uploadProfilePicture(uid, uploadSource)
            val latest = userDao.getById(uid)?.toDomain() ?: return@runCatching false
            updateUser(latest.copy(profilePictureUrl = url))
            // Fan the canonical HTTPS URL out to every hike this user created
            // (Room + Firestore) so their cards show it on every device.
            hikeLogRepository.syncCreatorInfo(uid, latest.username, url, pushRemote = true)
            true
        }
            .onFailure { Log.w(TAG, "Profile picture upload skipped", it) }
            .getOrDefault(false)
    }

    suspend fun search(query: String): List<User> =
        userDao.searchByUsername(query).map { it.toDomain() }

    private companion object { const val TAG = "UserRepository" }
}
