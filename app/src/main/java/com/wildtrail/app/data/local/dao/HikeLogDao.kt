package com.wildtrail.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wildtrail.app.data.local.entity.HikeLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HikeLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(hike: HikeLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(hikes: List<HikeLogEntity>)

    @Query("SELECT * FROM hike_logs WHERE hikeId = :id LIMIT 1")
    suspend fun getById(id: String): HikeLogEntity?

    @Query("SELECT * FROM hike_logs WHERE hikeId = :id LIMIT 1")
    fun observeById(id: String): Flow<HikeLogEntity?>

    /** A user's own hikes, newest first. */
    @Query("SELECT * FROM hike_logs WHERE creatorFirebaseUid = :uid ORDER BY endedAt DESC")
    fun observeByCreator(uid: String): Flow<List<HikeLogEntity>>

    /** One-shot variant used by the backfill path that patches the
     *  denormalised creator info on legacy hikes. */
    @Query("SELECT * FROM hike_logs WHERE creatorFirebaseUid = :uid")
    suspend fun getByCreator(uid: String): List<HikeLogEntity>

    /**
     * Hikes the user has liked, most-recently-liked first. We don't need a
     * new table — the existing normalised `likes` join table already records
     * every (user, hike) like, so we just INNER JOIN it onto `hike_logs`.
     */
    @Query(
        """
        SELECT h.* FROM hike_logs h
         INNER JOIN likes l ON l.hikeId = h.hikeId
         WHERE l.userUid = :uid
         ORDER BY l.createdAt DESC
        """,
    )
    fun observeLikedHikes(uid: String): Flow<List<HikeLogEntity>>

    /** "Explore" feed: only public hikes, most recent first. */
    @Query("SELECT * FROM hike_logs WHERE isPrivate = 0 ORDER BY endedAt DESC LIMIT :limit")
    fun observePublicFeed(limit: Int = 50): Flow<List<HikeLogEntity>>

    @Query(
        """
        SELECT * FROM hike_logs
         WHERE isPrivate = 0
           AND (title LIKE '%' || :q || '%' OR description LIKE '%' || :q || '%')
         ORDER BY likesCount DESC
         LIMIT 100
        """,
    )
    suspend fun search(q: String): List<HikeLogEntity>

    @Query("DELETE FROM hike_logs WHERE hikeId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM hike_logs")
    suspend fun clear()
}
