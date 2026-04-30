package com.wildtrail.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wildtrail.app.data.local.converter.Converters
import com.wildtrail.app.data.local.dao.AchievementDao
import com.wildtrail.app.data.local.dao.EmergencyContactDao
import com.wildtrail.app.data.local.dao.FollowedTrailDao
import com.wildtrail.app.data.local.dao.HikeCommentDao
import com.wildtrail.app.data.local.dao.HikeLogDao
import com.wildtrail.app.data.local.dao.TrailReviewDao
import com.wildtrail.app.data.local.dao.UserDao
import com.wildtrail.app.data.local.dao.UserFollowDao
import com.wildtrail.app.data.local.entity.AchievementDefinitionEntity
import com.wildtrail.app.data.local.entity.EmergencyContactEntity
import com.wildtrail.app.data.local.entity.FollowedTrailEntity
import com.wildtrail.app.data.local.entity.HikeCommentEntity
import com.wildtrail.app.data.local.entity.HikeLogEntity
import com.wildtrail.app.data.local.entity.TrailReviewEntity
import com.wildtrail.app.data.local.entity.UserAchievementEntity
import com.wildtrail.app.data.local.entity.UserEntity
import com.wildtrail.app.data.local.entity.UserFollowEntity

/**
 * The Room database for WildTrail.
 *
 *  - Owns ALL the local entities (mirroring the original schema sketch).
 *  - exportSchema = true puts a JSON snapshot in `app/schemas/` per version.
 *    This is what we'll diff against when we add migrations.
 *
 * IMPORTANT: bump [VERSION] every time you change any entity, and add a
 * Migration object in the [Migrations] file. For the assignment we ship at
 * v1 — no migrations needed yet.
 */
@Database(
    version = WildTrailDatabase.VERSION,
    exportSchema = true,
    entities = [
        UserEntity::class,
        HikeLogEntity::class,
        TrailReviewEntity::class,
        UserFollowEntity::class,
        FollowedTrailEntity::class,
        HikeCommentEntity::class,
        AchievementDefinitionEntity::class,
        UserAchievementEntity::class,
        EmergencyContactEntity::class,
    ],
)
@TypeConverters(Converters::class)
abstract class WildTrailDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun hikeLogDao(): HikeLogDao
    abstract fun trailReviewDao(): TrailReviewDao
    abstract fun userFollowDao(): UserFollowDao
    abstract fun followedTrailDao(): FollowedTrailDao
    abstract fun hikeCommentDao(): HikeCommentDao
    abstract fun achievementDao(): AchievementDao
    abstract fun emergencyContactDao(): EmergencyContactDao

    companion object {
        const val VERSION = 1
        private const val DB_NAME = "wildtrail.db"

        @Volatile
        private var instance: WildTrailDatabase? = null

        /** Lazy singleton — Room itself is thread-safe but constructing it twice
         *  would leak file handles. */
        fun getInstance(context: Context): WildTrailDatabase = instance ?: synchronized(this) {
            instance ?: build(context).also { instance = it }
        }

        private fun build(context: Context): WildTrailDatabase = Room.databaseBuilder(
            context.applicationContext,
            WildTrailDatabase::class.java,
            DB_NAME,
        )
            // For a uni demo we use destructive fallback so the app never crashes
            // on a schema mismatch in dev. In production you'd write Migrations
            // and remove this line.
            .fallbackToDestructiveMigration()
            .build()
    }
}
