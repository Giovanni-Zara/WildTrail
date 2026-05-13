package com.wildtrail.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wildtrail.app.data.local.entity.LikeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LikeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(like: LikeEntity)

    @Query("DELETE FROM likes WHERE userUid = :uid AND hikeId = :hikeId")
    suspend fun delete(uid: String, hikeId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE userUid = :uid AND hikeId = :hikeId)")
    fun observeIsLikedByUser(uid: String, hikeId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM likes WHERE hikeId = :hikeId")
    fun observeLikeCount(hikeId: String): Flow<Int>

    /** Used by feed screens to know which cards should show a filled heart. */
    @Query("SELECT hikeId FROM likes WHERE userUid = :uid")
    fun observeMyLikedHikeIds(uid: String): Flow<List<String>>
}
