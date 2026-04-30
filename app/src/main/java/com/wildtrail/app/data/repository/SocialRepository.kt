package com.wildtrail.app.data.repository

import android.util.Log
import com.wildtrail.app.data.local.dao.FollowedTrailDao
import com.wildtrail.app.data.local.dao.HikeCommentDao
import com.wildtrail.app.data.local.dao.TrailReviewDao
import com.wildtrail.app.data.local.dao.UserFollowDao
import com.wildtrail.app.data.local.entity.toDomain
import com.wildtrail.app.data.local.entity.toEntity
import com.wildtrail.app.data.remote.FirestoreService
import com.wildtrail.app.data.remote.dto.toDomain
import com.wildtrail.app.data.remote.dto.toDto
import com.wildtrail.app.domain.model.FollowedTrail
import com.wildtrail.app.domain.model.HikeComment
import com.wildtrail.app.domain.model.TrailReview
import com.wildtrail.app.domain.model.UserFollow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Aggregates everything social: reviews, user-follows, trail-follows, comments.
 * Same offline-first pattern as the other repositories. Firestore listener
 * errors are caught + logged so they never crash the app.
 */
class SocialRepository(
    private val reviewDao: TrailReviewDao,
    private val userFollowDao: UserFollowDao,
    private val followedTrailDao: FollowedTrailDao,
    private val commentDao: HikeCommentDao,
    private val firestore: FirestoreService,
    private val externalScope: CoroutineScope,
) {

    // --- Reviews ---------------------------------------------------------

    fun observeReviewsForHike(hikeId: String): Flow<List<TrailReview>> {
        firestore.observeReviewsForHike(hikeId)
            .catch { Log.w(TAG, "Firestore reviews listener errored", it) }
            .onEach { dtos -> dtos.forEach { reviewDao.upsert(it.toDomain().toEntity()) } }
            .launchIn(externalScope)
        return reviewDao.observeForHike(hikeId).map { list -> list.map { it.toDomain() } }
    }

    suspend fun submitReview(review: TrailReview) {
        reviewDao.upsert(review.toEntity())
        runCatching { firestore.upsertReview(review.toDto()) }
            .onFailure { Log.w(TAG, "Firestore review submit skipped", it) }
    }

    fun observeAvgDifficulty(hikeId: String) = reviewDao.observeAvgDifficulty(hikeId)

    // --- User follows ----------------------------------------------------

    suspend fun follow(follow: UserFollow) {
        userFollowDao.upsert(follow.toEntity())
        runCatching { firestore.followUser(follow.toDto()) }
            .onFailure { Log.w(TAG, "Firestore follow skipped", it) }
    }

    suspend fun unfollow(followerUid: String, followeeUid: String) {
        userFollowDao.delete(followerUid, followeeUid)
        runCatching { firestore.unfollowUser(followerUid, followeeUid) }
            .onFailure { Log.w(TAG, "Firestore unfollow skipped", it) }
    }

    fun observeIsFollowing(follower: String, followee: String) =
        userFollowDao.observeIsFollowing(follower, followee)

    // --- Trail follows ---------------------------------------------------

    suspend fun followTrail(trail: FollowedTrail) {
        followedTrailDao.upsert(trail.toEntity())
        runCatching { firestore.followTrail(trail.toDto()) }
            .onFailure { Log.w(TAG, "Firestore follow-trail skipped", it) }
    }

    suspend fun unfollowTrail(uid: String, hikeId: String) {
        followedTrailDao.delete(uid, hikeId)
        runCatching { firestore.unfollowTrail(uid, hikeId) }
            .onFailure { Log.w(TAG, "Firestore unfollow-trail skipped", it) }
    }

    fun observeFollowedTrails(uid: String): Flow<List<FollowedTrail>> =
        followedTrailDao.observeForUser(uid).map { list -> list.map { it.toDomain() } }

    // --- Comments --------------------------------------------------------

    fun observeComments(hikeId: String): Flow<List<HikeComment>> {
        firestore.observeCommentsForHike(hikeId)
            .catch { Log.w(TAG, "Firestore comments listener errored", it) }
            .onEach { dtos -> commentDao.upsertAll(dtos.map { it.toDomain().toEntity() }) }
            .launchIn(externalScope)
        return commentDao.observeForHike(hikeId).map { list -> list.map { it.toDomain() } }
    }

    suspend fun postComment(comment: HikeComment) {
        commentDao.upsert(comment.toEntity())
        runCatching { firestore.upsertComment(comment.toDto()) }
            .onFailure { Log.w(TAG, "Firestore comment post skipped", it) }
    }

    private companion object { const val TAG = "SocialRepository" }
}
