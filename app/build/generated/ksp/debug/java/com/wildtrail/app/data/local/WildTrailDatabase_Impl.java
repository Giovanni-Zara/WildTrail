package com.wildtrail.app.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.wildtrail.app.data.local.dao.AchievementDao;
import com.wildtrail.app.data.local.dao.AchievementDao_Impl;
import com.wildtrail.app.data.local.dao.EmergencyContactDao;
import com.wildtrail.app.data.local.dao.EmergencyContactDao_Impl;
import com.wildtrail.app.data.local.dao.FollowedTrailDao;
import com.wildtrail.app.data.local.dao.FollowedTrailDao_Impl;
import com.wildtrail.app.data.local.dao.HikeCommentDao;
import com.wildtrail.app.data.local.dao.HikeCommentDao_Impl;
import com.wildtrail.app.data.local.dao.HikeLogDao;
import com.wildtrail.app.data.local.dao.HikeLogDao_Impl;
import com.wildtrail.app.data.local.dao.TrailReviewDao;
import com.wildtrail.app.data.local.dao.TrailReviewDao_Impl;
import com.wildtrail.app.data.local.dao.UserDao;
import com.wildtrail.app.data.local.dao.UserDao_Impl;
import com.wildtrail.app.data.local.dao.UserFollowDao;
import com.wildtrail.app.data.local.dao.UserFollowDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class WildTrailDatabase_Impl extends WildTrailDatabase {
  private volatile UserDao _userDao;

  private volatile HikeLogDao _hikeLogDao;

  private volatile TrailReviewDao _trailReviewDao;

  private volatile UserFollowDao _userFollowDao;

  private volatile FollowedTrailDao _followedTrailDao;

  private volatile HikeCommentDao _hikeCommentDao;

  private volatile AchievementDao _achievementDao;

  private volatile EmergencyContactDao _emergencyContactDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`firebaseUid` TEXT NOT NULL, `username` TEXT NOT NULL, `age` INTEGER, `country` TEXT, `level` INTEGER NOT NULL, `xpPoints` INTEGER NOT NULL, `totalDistanceKm` REAL NOT NULL, `totalHikesCount` INTEGER NOT NULL, `profilePictureUrl` TEXT, `bio` TEXT, `createdAt` INTEGER NOT NULL, `lastActive` INTEGER NOT NULL, `isPublic` INTEGER NOT NULL, PRIMARY KEY(`firebaseUid`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `hike_logs` (`hikeId` TEXT NOT NULL, `creatorFirebaseUid` TEXT NOT NULL, `workoutId` TEXT, `title` TEXT NOT NULL, `description` TEXT, `avgSpeedKmh` REAL NOT NULL, `stepCount` INTEGER NOT NULL, `caloriesBurned` INTEGER NOT NULL, `coverPhotoUrl` TEXT, `xpEarned` INTEGER NOT NULL, `likesCount` INTEGER NOT NULL, `surfaceType` TEXT NOT NULL, `lengthKm` REAL NOT NULL, `durationSeconds` INTEGER NOT NULL, `startedAt` INTEGER NOT NULL, `endedAt` INTEGER NOT NULL, `elevationGainMeters` INTEGER NOT NULL, `routeCoordinates` TEXT NOT NULL, `isPrivate` INTEGER NOT NULL, PRIMARY KEY(`hikeId`), FOREIGN KEY(`creatorFirebaseUid`) REFERENCES `users`(`firebaseUid`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hike_logs_creatorFirebaseUid` ON `hike_logs` (`creatorFirebaseUid`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hike_logs_isPrivate` ON `hike_logs` (`isPrivate`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `trail_reviews` (`reviewId` TEXT NOT NULL, `reviewerUid` TEXT NOT NULL, `hikeId` TEXT NOT NULL, `fatigueLevel` INTEGER NOT NULL, `pathClarity` INTEGER NOT NULL, `difficultyLevel` INTEGER NOT NULL, `mudRisk` INTEGER NOT NULL, `animalEncounterRisk` INTEGER NOT NULL, `waterAvailability` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`reviewId`), FOREIGN KEY(`reviewerUid`) REFERENCES `users`(`firebaseUid`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`hikeId`) REFERENCES `hike_logs`(`hikeId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_trail_reviews_reviewerUid_hikeId` ON `trail_reviews` (`reviewerUid`, `hikeId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_trail_reviews_hikeId` ON `trail_reviews` (`hikeId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_follows` (`followerUid` TEXT NOT NULL, `followeeUid` TEXT NOT NULL, `notifyOnNewHike` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`followerUid`, `followeeUid`), FOREIGN KEY(`followerUid`) REFERENCES `users`(`firebaseUid`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`followeeUid`) REFERENCES `users`(`firebaseUid`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_user_follows_followeeUid` ON `user_follows` (`followeeUid`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `followed_trails` (`userUid` TEXT NOT NULL, `hikeId` TEXT NOT NULL, `notifyOnNewReview` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`userUid`, `hikeId`), FOREIGN KEY(`userUid`) REFERENCES `users`(`firebaseUid`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`hikeId`) REFERENCES `hike_logs`(`hikeId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_followed_trails_hikeId` ON `followed_trails` (`hikeId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `hike_comments` (`commentId` TEXT NOT NULL, `authorUid` TEXT NOT NULL, `hikeId` TEXT NOT NULL, `text` TEXT NOT NULL, `photoUrls` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`commentId`), FOREIGN KEY(`authorUid`) REFERENCES `users`(`firebaseUid`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`hikeId`) REFERENCES `hike_logs`(`hikeId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hike_comments_hikeId` ON `hike_comments` (`hikeId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hike_comments_authorUid` ON `hike_comments` (`authorUid`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `achievement_definitions` (`achievementId` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `iconUrl` TEXT, `xpReward` INTEGER NOT NULL, `category` TEXT NOT NULL, `thresholdValue` REAL NOT NULL, PRIMARY KEY(`achievementId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_achievements` (`userUid` TEXT NOT NULL, `achievementId` TEXT NOT NULL, `earnedAt` INTEGER NOT NULL, PRIMARY KEY(`userUid`, `achievementId`), FOREIGN KEY(`userUid`) REFERENCES `users`(`firebaseUid`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`achievementId`) REFERENCES `achievement_definitions`(`achievementId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_user_achievements_achievementId` ON `user_achievements` (`achievementId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `emergency_contacts` (`contactId` TEXT NOT NULL, `userUid` TEXT NOT NULL, `name` TEXT NOT NULL, `phoneNumber` TEXT NOT NULL, `relationship` TEXT, `isPrimary` INTEGER NOT NULL, `notifyOnFall` INTEGER NOT NULL, PRIMARY KEY(`contactId`), FOREIGN KEY(`userUid`) REFERENCES `users`(`firebaseUid`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_emergency_contacts_userUid` ON `emergency_contacts` (`userUid`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '95f3d47eef37973ed18556581a47ca68')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `users`");
        db.execSQL("DROP TABLE IF EXISTS `hike_logs`");
        db.execSQL("DROP TABLE IF EXISTS `trail_reviews`");
        db.execSQL("DROP TABLE IF EXISTS `user_follows`");
        db.execSQL("DROP TABLE IF EXISTS `followed_trails`");
        db.execSQL("DROP TABLE IF EXISTS `hike_comments`");
        db.execSQL("DROP TABLE IF EXISTS `achievement_definitions`");
        db.execSQL("DROP TABLE IF EXISTS `user_achievements`");
        db.execSQL("DROP TABLE IF EXISTS `emergency_contacts`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(13);
        _columnsUsers.put("firebaseUid", new TableInfo.Column("firebaseUid", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("username", new TableInfo.Column("username", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("age", new TableInfo.Column("age", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("country", new TableInfo.Column("country", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("level", new TableInfo.Column("level", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("xpPoints", new TableInfo.Column("xpPoints", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("totalDistanceKm", new TableInfo.Column("totalDistanceKm", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("totalHikesCount", new TableInfo.Column("totalHikesCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("profilePictureUrl", new TableInfo.Column("profilePictureUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("bio", new TableInfo.Column("bio", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("lastActive", new TableInfo.Column("lastActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("isPublic", new TableInfo.Column("isPublic", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(db, "users");
        if (!_infoUsers.equals(_existingUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "users(com.wildtrail.app.data.local.entity.UserEntity).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsHikeLogs = new HashMap<String, TableInfo.Column>(19);
        _columnsHikeLogs.put("hikeId", new TableInfo.Column("hikeId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("creatorFirebaseUid", new TableInfo.Column("creatorFirebaseUid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("workoutId", new TableInfo.Column("workoutId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("avgSpeedKmh", new TableInfo.Column("avgSpeedKmh", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("stepCount", new TableInfo.Column("stepCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("caloriesBurned", new TableInfo.Column("caloriesBurned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("coverPhotoUrl", new TableInfo.Column("coverPhotoUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("xpEarned", new TableInfo.Column("xpEarned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("likesCount", new TableInfo.Column("likesCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("surfaceType", new TableInfo.Column("surfaceType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("lengthKm", new TableInfo.Column("lengthKm", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("durationSeconds", new TableInfo.Column("durationSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("startedAt", new TableInfo.Column("startedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("endedAt", new TableInfo.Column("endedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("elevationGainMeters", new TableInfo.Column("elevationGainMeters", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("routeCoordinates", new TableInfo.Column("routeCoordinates", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeLogs.put("isPrivate", new TableInfo.Column("isPrivate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHikeLogs = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysHikeLogs.add(new TableInfo.ForeignKey("users", "CASCADE", "NO ACTION", Arrays.asList("creatorFirebaseUid"), Arrays.asList("firebaseUid")));
        final HashSet<TableInfo.Index> _indicesHikeLogs = new HashSet<TableInfo.Index>(2);
        _indicesHikeLogs.add(new TableInfo.Index("index_hike_logs_creatorFirebaseUid", false, Arrays.asList("creatorFirebaseUid"), Arrays.asList("ASC")));
        _indicesHikeLogs.add(new TableInfo.Index("index_hike_logs_isPrivate", false, Arrays.asList("isPrivate"), Arrays.asList("ASC")));
        final TableInfo _infoHikeLogs = new TableInfo("hike_logs", _columnsHikeLogs, _foreignKeysHikeLogs, _indicesHikeLogs);
        final TableInfo _existingHikeLogs = TableInfo.read(db, "hike_logs");
        if (!_infoHikeLogs.equals(_existingHikeLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "hike_logs(com.wildtrail.app.data.local.entity.HikeLogEntity).\n"
                  + " Expected:\n" + _infoHikeLogs + "\n"
                  + " Found:\n" + _existingHikeLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsTrailReviews = new HashMap<String, TableInfo.Column>(10);
        _columnsTrailReviews.put("reviewId", new TableInfo.Column("reviewId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrailReviews.put("reviewerUid", new TableInfo.Column("reviewerUid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrailReviews.put("hikeId", new TableInfo.Column("hikeId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrailReviews.put("fatigueLevel", new TableInfo.Column("fatigueLevel", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrailReviews.put("pathClarity", new TableInfo.Column("pathClarity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrailReviews.put("difficultyLevel", new TableInfo.Column("difficultyLevel", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrailReviews.put("mudRisk", new TableInfo.Column("mudRisk", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrailReviews.put("animalEncounterRisk", new TableInfo.Column("animalEncounterRisk", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrailReviews.put("waterAvailability", new TableInfo.Column("waterAvailability", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrailReviews.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTrailReviews = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysTrailReviews.add(new TableInfo.ForeignKey("users", "CASCADE", "NO ACTION", Arrays.asList("reviewerUid"), Arrays.asList("firebaseUid")));
        _foreignKeysTrailReviews.add(new TableInfo.ForeignKey("hike_logs", "CASCADE", "NO ACTION", Arrays.asList("hikeId"), Arrays.asList("hikeId")));
        final HashSet<TableInfo.Index> _indicesTrailReviews = new HashSet<TableInfo.Index>(2);
        _indicesTrailReviews.add(new TableInfo.Index("index_trail_reviews_reviewerUid_hikeId", true, Arrays.asList("reviewerUid", "hikeId"), Arrays.asList("ASC", "ASC")));
        _indicesTrailReviews.add(new TableInfo.Index("index_trail_reviews_hikeId", false, Arrays.asList("hikeId"), Arrays.asList("ASC")));
        final TableInfo _infoTrailReviews = new TableInfo("trail_reviews", _columnsTrailReviews, _foreignKeysTrailReviews, _indicesTrailReviews);
        final TableInfo _existingTrailReviews = TableInfo.read(db, "trail_reviews");
        if (!_infoTrailReviews.equals(_existingTrailReviews)) {
          return new RoomOpenHelper.ValidationResult(false, "trail_reviews(com.wildtrail.app.data.local.entity.TrailReviewEntity).\n"
                  + " Expected:\n" + _infoTrailReviews + "\n"
                  + " Found:\n" + _existingTrailReviews);
        }
        final HashMap<String, TableInfo.Column> _columnsUserFollows = new HashMap<String, TableInfo.Column>(4);
        _columnsUserFollows.put("followerUid", new TableInfo.Column("followerUid", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserFollows.put("followeeUid", new TableInfo.Column("followeeUid", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserFollows.put("notifyOnNewHike", new TableInfo.Column("notifyOnNewHike", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserFollows.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserFollows = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysUserFollows.add(new TableInfo.ForeignKey("users", "CASCADE", "NO ACTION", Arrays.asList("followerUid"), Arrays.asList("firebaseUid")));
        _foreignKeysUserFollows.add(new TableInfo.ForeignKey("users", "CASCADE", "NO ACTION", Arrays.asList("followeeUid"), Arrays.asList("firebaseUid")));
        final HashSet<TableInfo.Index> _indicesUserFollows = new HashSet<TableInfo.Index>(1);
        _indicesUserFollows.add(new TableInfo.Index("index_user_follows_followeeUid", false, Arrays.asList("followeeUid"), Arrays.asList("ASC")));
        final TableInfo _infoUserFollows = new TableInfo("user_follows", _columnsUserFollows, _foreignKeysUserFollows, _indicesUserFollows);
        final TableInfo _existingUserFollows = TableInfo.read(db, "user_follows");
        if (!_infoUserFollows.equals(_existingUserFollows)) {
          return new RoomOpenHelper.ValidationResult(false, "user_follows(com.wildtrail.app.data.local.entity.UserFollowEntity).\n"
                  + " Expected:\n" + _infoUserFollows + "\n"
                  + " Found:\n" + _existingUserFollows);
        }
        final HashMap<String, TableInfo.Column> _columnsFollowedTrails = new HashMap<String, TableInfo.Column>(4);
        _columnsFollowedTrails.put("userUid", new TableInfo.Column("userUid", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFollowedTrails.put("hikeId", new TableInfo.Column("hikeId", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFollowedTrails.put("notifyOnNewReview", new TableInfo.Column("notifyOnNewReview", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFollowedTrails.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFollowedTrails = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysFollowedTrails.add(new TableInfo.ForeignKey("users", "CASCADE", "NO ACTION", Arrays.asList("userUid"), Arrays.asList("firebaseUid")));
        _foreignKeysFollowedTrails.add(new TableInfo.ForeignKey("hike_logs", "CASCADE", "NO ACTION", Arrays.asList("hikeId"), Arrays.asList("hikeId")));
        final HashSet<TableInfo.Index> _indicesFollowedTrails = new HashSet<TableInfo.Index>(1);
        _indicesFollowedTrails.add(new TableInfo.Index("index_followed_trails_hikeId", false, Arrays.asList("hikeId"), Arrays.asList("ASC")));
        final TableInfo _infoFollowedTrails = new TableInfo("followed_trails", _columnsFollowedTrails, _foreignKeysFollowedTrails, _indicesFollowedTrails);
        final TableInfo _existingFollowedTrails = TableInfo.read(db, "followed_trails");
        if (!_infoFollowedTrails.equals(_existingFollowedTrails)) {
          return new RoomOpenHelper.ValidationResult(false, "followed_trails(com.wildtrail.app.data.local.entity.FollowedTrailEntity).\n"
                  + " Expected:\n" + _infoFollowedTrails + "\n"
                  + " Found:\n" + _existingFollowedTrails);
        }
        final HashMap<String, TableInfo.Column> _columnsHikeComments = new HashMap<String, TableInfo.Column>(6);
        _columnsHikeComments.put("commentId", new TableInfo.Column("commentId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeComments.put("authorUid", new TableInfo.Column("authorUid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeComments.put("hikeId", new TableInfo.Column("hikeId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeComments.put("text", new TableInfo.Column("text", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeComments.put("photoUrls", new TableInfo.Column("photoUrls", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHikeComments.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHikeComments = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysHikeComments.add(new TableInfo.ForeignKey("users", "CASCADE", "NO ACTION", Arrays.asList("authorUid"), Arrays.asList("firebaseUid")));
        _foreignKeysHikeComments.add(new TableInfo.ForeignKey("hike_logs", "CASCADE", "NO ACTION", Arrays.asList("hikeId"), Arrays.asList("hikeId")));
        final HashSet<TableInfo.Index> _indicesHikeComments = new HashSet<TableInfo.Index>(2);
        _indicesHikeComments.add(new TableInfo.Index("index_hike_comments_hikeId", false, Arrays.asList("hikeId"), Arrays.asList("ASC")));
        _indicesHikeComments.add(new TableInfo.Index("index_hike_comments_authorUid", false, Arrays.asList("authorUid"), Arrays.asList("ASC")));
        final TableInfo _infoHikeComments = new TableInfo("hike_comments", _columnsHikeComments, _foreignKeysHikeComments, _indicesHikeComments);
        final TableInfo _existingHikeComments = TableInfo.read(db, "hike_comments");
        if (!_infoHikeComments.equals(_existingHikeComments)) {
          return new RoomOpenHelper.ValidationResult(false, "hike_comments(com.wildtrail.app.data.local.entity.HikeCommentEntity).\n"
                  + " Expected:\n" + _infoHikeComments + "\n"
                  + " Found:\n" + _existingHikeComments);
        }
        final HashMap<String, TableInfo.Column> _columnsAchievementDefinitions = new HashMap<String, TableInfo.Column>(7);
        _columnsAchievementDefinitions.put("achievementId", new TableInfo.Column("achievementId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievementDefinitions.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievementDefinitions.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievementDefinitions.put("iconUrl", new TableInfo.Column("iconUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievementDefinitions.put("xpReward", new TableInfo.Column("xpReward", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievementDefinitions.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAchievementDefinitions.put("thresholdValue", new TableInfo.Column("thresholdValue", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAchievementDefinitions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAchievementDefinitions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAchievementDefinitions = new TableInfo("achievement_definitions", _columnsAchievementDefinitions, _foreignKeysAchievementDefinitions, _indicesAchievementDefinitions);
        final TableInfo _existingAchievementDefinitions = TableInfo.read(db, "achievement_definitions");
        if (!_infoAchievementDefinitions.equals(_existingAchievementDefinitions)) {
          return new RoomOpenHelper.ValidationResult(false, "achievement_definitions(com.wildtrail.app.data.local.entity.AchievementDefinitionEntity).\n"
                  + " Expected:\n" + _infoAchievementDefinitions + "\n"
                  + " Found:\n" + _existingAchievementDefinitions);
        }
        final HashMap<String, TableInfo.Column> _columnsUserAchievements = new HashMap<String, TableInfo.Column>(3);
        _columnsUserAchievements.put("userUid", new TableInfo.Column("userUid", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserAchievements.put("achievementId", new TableInfo.Column("achievementId", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserAchievements.put("earnedAt", new TableInfo.Column("earnedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserAchievements = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysUserAchievements.add(new TableInfo.ForeignKey("users", "CASCADE", "NO ACTION", Arrays.asList("userUid"), Arrays.asList("firebaseUid")));
        _foreignKeysUserAchievements.add(new TableInfo.ForeignKey("achievement_definitions", "CASCADE", "NO ACTION", Arrays.asList("achievementId"), Arrays.asList("achievementId")));
        final HashSet<TableInfo.Index> _indicesUserAchievements = new HashSet<TableInfo.Index>(1);
        _indicesUserAchievements.add(new TableInfo.Index("index_user_achievements_achievementId", false, Arrays.asList("achievementId"), Arrays.asList("ASC")));
        final TableInfo _infoUserAchievements = new TableInfo("user_achievements", _columnsUserAchievements, _foreignKeysUserAchievements, _indicesUserAchievements);
        final TableInfo _existingUserAchievements = TableInfo.read(db, "user_achievements");
        if (!_infoUserAchievements.equals(_existingUserAchievements)) {
          return new RoomOpenHelper.ValidationResult(false, "user_achievements(com.wildtrail.app.data.local.entity.UserAchievementEntity).\n"
                  + " Expected:\n" + _infoUserAchievements + "\n"
                  + " Found:\n" + _existingUserAchievements);
        }
        final HashMap<String, TableInfo.Column> _columnsEmergencyContacts = new HashMap<String, TableInfo.Column>(7);
        _columnsEmergencyContacts.put("contactId", new TableInfo.Column("contactId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("userUid", new TableInfo.Column("userUid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("phoneNumber", new TableInfo.Column("phoneNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("relationship", new TableInfo.Column("relationship", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("isPrimary", new TableInfo.Column("isPrimary", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("notifyOnFall", new TableInfo.Column("notifyOnFall", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEmergencyContacts = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysEmergencyContacts.add(new TableInfo.ForeignKey("users", "CASCADE", "NO ACTION", Arrays.asList("userUid"), Arrays.asList("firebaseUid")));
        final HashSet<TableInfo.Index> _indicesEmergencyContacts = new HashSet<TableInfo.Index>(1);
        _indicesEmergencyContacts.add(new TableInfo.Index("index_emergency_contacts_userUid", false, Arrays.asList("userUid"), Arrays.asList("ASC")));
        final TableInfo _infoEmergencyContacts = new TableInfo("emergency_contacts", _columnsEmergencyContacts, _foreignKeysEmergencyContacts, _indicesEmergencyContacts);
        final TableInfo _existingEmergencyContacts = TableInfo.read(db, "emergency_contacts");
        if (!_infoEmergencyContacts.equals(_existingEmergencyContacts)) {
          return new RoomOpenHelper.ValidationResult(false, "emergency_contacts(com.wildtrail.app.data.local.entity.EmergencyContactEntity).\n"
                  + " Expected:\n" + _infoEmergencyContacts + "\n"
                  + " Found:\n" + _existingEmergencyContacts);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "95f3d47eef37973ed18556581a47ca68", "7c341ce2cd959b4468ddef01767140f9");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "users","hike_logs","trail_reviews","user_follows","followed_trails","hike_comments","achievement_definitions","user_achievements","emergency_contacts");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `users`");
      _db.execSQL("DELETE FROM `hike_logs`");
      _db.execSQL("DELETE FROM `trail_reviews`");
      _db.execSQL("DELETE FROM `user_follows`");
      _db.execSQL("DELETE FROM `followed_trails`");
      _db.execSQL("DELETE FROM `hike_comments`");
      _db.execSQL("DELETE FROM `achievement_definitions`");
      _db.execSQL("DELETE FROM `user_achievements`");
      _db.execSQL("DELETE FROM `emergency_contacts`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(HikeLogDao.class, HikeLogDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TrailReviewDao.class, TrailReviewDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UserFollowDao.class, UserFollowDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FollowedTrailDao.class, FollowedTrailDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(HikeCommentDao.class, HikeCommentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AchievementDao.class, AchievementDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(EmergencyContactDao.class, EmergencyContactDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public HikeLogDao hikeLogDao() {
    if (_hikeLogDao != null) {
      return _hikeLogDao;
    } else {
      synchronized(this) {
        if(_hikeLogDao == null) {
          _hikeLogDao = new HikeLogDao_Impl(this);
        }
        return _hikeLogDao;
      }
    }
  }

  @Override
  public TrailReviewDao trailReviewDao() {
    if (_trailReviewDao != null) {
      return _trailReviewDao;
    } else {
      synchronized(this) {
        if(_trailReviewDao == null) {
          _trailReviewDao = new TrailReviewDao_Impl(this);
        }
        return _trailReviewDao;
      }
    }
  }

  @Override
  public UserFollowDao userFollowDao() {
    if (_userFollowDao != null) {
      return _userFollowDao;
    } else {
      synchronized(this) {
        if(_userFollowDao == null) {
          _userFollowDao = new UserFollowDao_Impl(this);
        }
        return _userFollowDao;
      }
    }
  }

  @Override
  public FollowedTrailDao followedTrailDao() {
    if (_followedTrailDao != null) {
      return _followedTrailDao;
    } else {
      synchronized(this) {
        if(_followedTrailDao == null) {
          _followedTrailDao = new FollowedTrailDao_Impl(this);
        }
        return _followedTrailDao;
      }
    }
  }

  @Override
  public HikeCommentDao hikeCommentDao() {
    if (_hikeCommentDao != null) {
      return _hikeCommentDao;
    } else {
      synchronized(this) {
        if(_hikeCommentDao == null) {
          _hikeCommentDao = new HikeCommentDao_Impl(this);
        }
        return _hikeCommentDao;
      }
    }
  }

  @Override
  public AchievementDao achievementDao() {
    if (_achievementDao != null) {
      return _achievementDao;
    } else {
      synchronized(this) {
        if(_achievementDao == null) {
          _achievementDao = new AchievementDao_Impl(this);
        }
        return _achievementDao;
      }
    }
  }

  @Override
  public EmergencyContactDao emergencyContactDao() {
    if (_emergencyContactDao != null) {
      return _emergencyContactDao;
    } else {
      synchronized(this) {
        if(_emergencyContactDao == null) {
          _emergencyContactDao = new EmergencyContactDao_Impl(this);
        }
        return _emergencyContactDao;
      }
    }
  }
}
