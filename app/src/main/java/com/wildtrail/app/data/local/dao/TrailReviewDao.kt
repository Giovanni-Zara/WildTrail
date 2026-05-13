package com.wildtrail.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wildtrail.app.data.local.entity.TrailReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrailReviewDao {

    /** REPLACE on (reviewerUid, hikeId) gives us the upsert behaviour we want
     *  — re-submitting overwrites the previous review for that hike. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(review: TrailReviewEntity)

    @Query("SELECT * FROM trail_reviews WHERE hikeId = :hikeId ORDER BY createdAt DESC")
    fun observeForHike(hikeId: String): Flow<List<TrailReviewEntity>>

    @Query("SELECT * FROM trail_reviews WHERE reviewerUid = :uid AND hikeId = :hikeId LIMIT 1")
    suspend fun getMine(uid: String, hikeId: String): TrailReviewEntity?

    @Query("SELECT AVG(difficultyLevel) FROM trail_reviews WHERE hikeId = :hikeId")
    fun observeAvgDifficulty(hikeId: String): Flow<Double?>

    @Query("SELECT AVG(overallRating) FROM trail_reviews WHERE hikeId = :hikeId")
    suspend fun getAvgOverallRating(hikeId: String): Double?

    @Query("SELECT COUNT(*) FROM trail_reviews WHERE hikeId = :hikeId")
    suspend fun getCount(hikeId: String): Int

    @Query("DELETE FROM trail_reviews WHERE reviewId = :id")
    suspend fun deleteById(id: String)
}
