package com.wildtrail.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.wildtrail.app.data.local.converter.Converters;
import com.wildtrail.app.data.local.entity.HikeLogEntity;
import com.wildtrail.app.domain.model.GeoPoint;
import com.wildtrail.app.domain.model.SurfaceType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class HikeLogDao_Impl implements HikeLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HikeLogEntity> __insertionAdapterOfHikeLogEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfClear;

  public HikeLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHikeLogEntity = new EntityInsertionAdapter<HikeLogEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `hike_logs` (`hikeId`,`creatorFirebaseUid`,`creatorUsername`,`creatorProfilePictureUrl`,`workoutId`,`title`,`description`,`avgSpeedKmh`,`stepCount`,`caloriesBurned`,`coverPhotoUrl`,`xpEarned`,`likesCount`,`surfaceType`,`lengthKm`,`durationSeconds`,`startedAt`,`endedAt`,`elevationGainMeters`,`routeCoordinates`,`isPrivate`,`difficultyLevel`,`mudRisk`,`pathClarity`,`fatigueLevel`,`animalEncounterRisk`,`waterAvailability`,`averageRating`,`reviewCount`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HikeLogEntity entity) {
        statement.bindString(1, entity.getHikeId());
        statement.bindString(2, entity.getCreatorFirebaseUid());
        statement.bindString(3, entity.getCreatorUsername());
        if (entity.getCreatorProfilePictureUrl() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCreatorProfilePictureUrl());
        }
        if (entity.getWorkoutId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getWorkoutId());
        }
        statement.bindString(6, entity.getTitle());
        if (entity.getDescription() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getDescription());
        }
        statement.bindDouble(8, entity.getAvgSpeedKmh());
        statement.bindLong(9, entity.getStepCount());
        statement.bindLong(10, entity.getCaloriesBurned());
        if (entity.getCoverPhotoUrl() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getCoverPhotoUrl());
        }
        statement.bindLong(12, entity.getXpEarned());
        statement.bindLong(13, entity.getLikesCount());
        final String _tmp = Converters.INSTANCE.surfaceTypeToString(entity.getSurfaceType());
        statement.bindString(14, _tmp);
        statement.bindDouble(15, entity.getLengthKm());
        statement.bindLong(16, entity.getDurationSeconds());
        statement.bindLong(17, entity.getStartedAt());
        statement.bindLong(18, entity.getEndedAt());
        statement.bindLong(19, entity.getElevationGainMeters());
        final String _tmp_1 = Converters.INSTANCE.geoPointListToJson(entity.getRouteCoordinates());
        statement.bindString(20, _tmp_1);
        final int _tmp_2 = entity.isPrivate() ? 1 : 0;
        statement.bindLong(21, _tmp_2);
        statement.bindLong(22, entity.getDifficultyLevel());
        statement.bindLong(23, entity.getMudRisk());
        statement.bindLong(24, entity.getPathClarity());
        statement.bindLong(25, entity.getFatigueLevel());
        statement.bindLong(26, entity.getAnimalEncounterRisk());
        final int _tmp_3 = entity.getWaterAvailability() ? 1 : 0;
        statement.bindLong(27, _tmp_3);
        statement.bindDouble(28, entity.getAverageRating());
        statement.bindLong(29, entity.getReviewCount());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM hike_logs WHERE hikeId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClear = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM hike_logs";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final HikeLogEntity hike, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfHikeLogEntity.insert(hike);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertAll(final List<HikeLogEntity> hikes,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfHikeLogEntity.insert(hikes);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clear(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClear.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClear.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getById(final String id, final Continuation<? super HikeLogEntity> $completion) {
    final String _sql = "SELECT * FROM hike_logs WHERE hikeId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<HikeLogEntity>() {
      @Override
      @Nullable
      public HikeLogEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHikeId = CursorUtil.getColumnIndexOrThrow(_cursor, "hikeId");
          final int _cursorIndexOfCreatorFirebaseUid = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorFirebaseUid");
          final int _cursorIndexOfCreatorUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorUsername");
          final int _cursorIndexOfCreatorProfilePictureUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorProfilePictureUrl");
          final int _cursorIndexOfWorkoutId = CursorUtil.getColumnIndexOrThrow(_cursor, "workoutId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAvgSpeedKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "avgSpeedKmh");
          final int _cursorIndexOfStepCount = CursorUtil.getColumnIndexOrThrow(_cursor, "stepCount");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfCoverPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "coverPhotoUrl");
          final int _cursorIndexOfXpEarned = CursorUtil.getColumnIndexOrThrow(_cursor, "xpEarned");
          final int _cursorIndexOfLikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "likesCount");
          final int _cursorIndexOfSurfaceType = CursorUtil.getColumnIndexOrThrow(_cursor, "surfaceType");
          final int _cursorIndexOfLengthKm = CursorUtil.getColumnIndexOrThrow(_cursor, "lengthKm");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final int _cursorIndexOfElevationGainMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "elevationGainMeters");
          final int _cursorIndexOfRouteCoordinates = CursorUtil.getColumnIndexOrThrow(_cursor, "routeCoordinates");
          final int _cursorIndexOfIsPrivate = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrivate");
          final int _cursorIndexOfDifficultyLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "difficultyLevel");
          final int _cursorIndexOfMudRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "mudRisk");
          final int _cursorIndexOfPathClarity = CursorUtil.getColumnIndexOrThrow(_cursor, "pathClarity");
          final int _cursorIndexOfFatigueLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "fatigueLevel");
          final int _cursorIndexOfAnimalEncounterRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "animalEncounterRisk");
          final int _cursorIndexOfWaterAvailability = CursorUtil.getColumnIndexOrThrow(_cursor, "waterAvailability");
          final int _cursorIndexOfAverageRating = CursorUtil.getColumnIndexOrThrow(_cursor, "averageRating");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final HikeLogEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpHikeId;
            _tmpHikeId = _cursor.getString(_cursorIndexOfHikeId);
            final String _tmpCreatorFirebaseUid;
            _tmpCreatorFirebaseUid = _cursor.getString(_cursorIndexOfCreatorFirebaseUid);
            final String _tmpCreatorUsername;
            _tmpCreatorUsername = _cursor.getString(_cursorIndexOfCreatorUsername);
            final String _tmpCreatorProfilePictureUrl;
            if (_cursor.isNull(_cursorIndexOfCreatorProfilePictureUrl)) {
              _tmpCreatorProfilePictureUrl = null;
            } else {
              _tmpCreatorProfilePictureUrl = _cursor.getString(_cursorIndexOfCreatorProfilePictureUrl);
            }
            final String _tmpWorkoutId;
            if (_cursor.isNull(_cursorIndexOfWorkoutId)) {
              _tmpWorkoutId = null;
            } else {
              _tmpWorkoutId = _cursor.getString(_cursorIndexOfWorkoutId);
            }
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final float _tmpAvgSpeedKmh;
            _tmpAvgSpeedKmh = _cursor.getFloat(_cursorIndexOfAvgSpeedKmh);
            final int _tmpStepCount;
            _tmpStepCount = _cursor.getInt(_cursorIndexOfStepCount);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            final String _tmpCoverPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfCoverPhotoUrl)) {
              _tmpCoverPhotoUrl = null;
            } else {
              _tmpCoverPhotoUrl = _cursor.getString(_cursorIndexOfCoverPhotoUrl);
            }
            final int _tmpXpEarned;
            _tmpXpEarned = _cursor.getInt(_cursorIndexOfXpEarned);
            final int _tmpLikesCount;
            _tmpLikesCount = _cursor.getInt(_cursorIndexOfLikesCount);
            final SurfaceType _tmpSurfaceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfSurfaceType);
            _tmpSurfaceType = Converters.INSTANCE.stringToSurfaceType(_tmp);
            final float _tmpLengthKm;
            _tmpLengthKm = _cursor.getFloat(_cursorIndexOfLengthKm);
            final long _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getLong(_cursorIndexOfDurationSeconds);
            final long _tmpStartedAt;
            _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            final long _tmpEndedAt;
            _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            final int _tmpElevationGainMeters;
            _tmpElevationGainMeters = _cursor.getInt(_cursorIndexOfElevationGainMeters);
            final List<GeoPoint> _tmpRouteCoordinates;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfRouteCoordinates);
            _tmpRouteCoordinates = Converters.INSTANCE.jsonToGeoPointList(_tmp_1);
            final boolean _tmpIsPrivate;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPrivate);
            _tmpIsPrivate = _tmp_2 != 0;
            final int _tmpDifficultyLevel;
            _tmpDifficultyLevel = _cursor.getInt(_cursorIndexOfDifficultyLevel);
            final int _tmpMudRisk;
            _tmpMudRisk = _cursor.getInt(_cursorIndexOfMudRisk);
            final int _tmpPathClarity;
            _tmpPathClarity = _cursor.getInt(_cursorIndexOfPathClarity);
            final int _tmpFatigueLevel;
            _tmpFatigueLevel = _cursor.getInt(_cursorIndexOfFatigueLevel);
            final int _tmpAnimalEncounterRisk;
            _tmpAnimalEncounterRisk = _cursor.getInt(_cursorIndexOfAnimalEncounterRisk);
            final boolean _tmpWaterAvailability;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfWaterAvailability);
            _tmpWaterAvailability = _tmp_3 != 0;
            final float _tmpAverageRating;
            _tmpAverageRating = _cursor.getFloat(_cursorIndexOfAverageRating);
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            _result = new HikeLogEntity(_tmpHikeId,_tmpCreatorFirebaseUid,_tmpCreatorUsername,_tmpCreatorProfilePictureUrl,_tmpWorkoutId,_tmpTitle,_tmpDescription,_tmpAvgSpeedKmh,_tmpStepCount,_tmpCaloriesBurned,_tmpCoverPhotoUrl,_tmpXpEarned,_tmpLikesCount,_tmpSurfaceType,_tmpLengthKm,_tmpDurationSeconds,_tmpStartedAt,_tmpEndedAt,_tmpElevationGainMeters,_tmpRouteCoordinates,_tmpIsPrivate,_tmpDifficultyLevel,_tmpMudRisk,_tmpPathClarity,_tmpFatigueLevel,_tmpAnimalEncounterRisk,_tmpWaterAvailability,_tmpAverageRating,_tmpReviewCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<HikeLogEntity> observeById(final String id) {
    final String _sql = "SELECT * FROM hike_logs WHERE hikeId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"hike_logs"}, new Callable<HikeLogEntity>() {
      @Override
      @Nullable
      public HikeLogEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHikeId = CursorUtil.getColumnIndexOrThrow(_cursor, "hikeId");
          final int _cursorIndexOfCreatorFirebaseUid = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorFirebaseUid");
          final int _cursorIndexOfCreatorUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorUsername");
          final int _cursorIndexOfCreatorProfilePictureUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorProfilePictureUrl");
          final int _cursorIndexOfWorkoutId = CursorUtil.getColumnIndexOrThrow(_cursor, "workoutId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAvgSpeedKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "avgSpeedKmh");
          final int _cursorIndexOfStepCount = CursorUtil.getColumnIndexOrThrow(_cursor, "stepCount");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfCoverPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "coverPhotoUrl");
          final int _cursorIndexOfXpEarned = CursorUtil.getColumnIndexOrThrow(_cursor, "xpEarned");
          final int _cursorIndexOfLikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "likesCount");
          final int _cursorIndexOfSurfaceType = CursorUtil.getColumnIndexOrThrow(_cursor, "surfaceType");
          final int _cursorIndexOfLengthKm = CursorUtil.getColumnIndexOrThrow(_cursor, "lengthKm");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final int _cursorIndexOfElevationGainMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "elevationGainMeters");
          final int _cursorIndexOfRouteCoordinates = CursorUtil.getColumnIndexOrThrow(_cursor, "routeCoordinates");
          final int _cursorIndexOfIsPrivate = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrivate");
          final int _cursorIndexOfDifficultyLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "difficultyLevel");
          final int _cursorIndexOfMudRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "mudRisk");
          final int _cursorIndexOfPathClarity = CursorUtil.getColumnIndexOrThrow(_cursor, "pathClarity");
          final int _cursorIndexOfFatigueLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "fatigueLevel");
          final int _cursorIndexOfAnimalEncounterRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "animalEncounterRisk");
          final int _cursorIndexOfWaterAvailability = CursorUtil.getColumnIndexOrThrow(_cursor, "waterAvailability");
          final int _cursorIndexOfAverageRating = CursorUtil.getColumnIndexOrThrow(_cursor, "averageRating");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final HikeLogEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpHikeId;
            _tmpHikeId = _cursor.getString(_cursorIndexOfHikeId);
            final String _tmpCreatorFirebaseUid;
            _tmpCreatorFirebaseUid = _cursor.getString(_cursorIndexOfCreatorFirebaseUid);
            final String _tmpCreatorUsername;
            _tmpCreatorUsername = _cursor.getString(_cursorIndexOfCreatorUsername);
            final String _tmpCreatorProfilePictureUrl;
            if (_cursor.isNull(_cursorIndexOfCreatorProfilePictureUrl)) {
              _tmpCreatorProfilePictureUrl = null;
            } else {
              _tmpCreatorProfilePictureUrl = _cursor.getString(_cursorIndexOfCreatorProfilePictureUrl);
            }
            final String _tmpWorkoutId;
            if (_cursor.isNull(_cursorIndexOfWorkoutId)) {
              _tmpWorkoutId = null;
            } else {
              _tmpWorkoutId = _cursor.getString(_cursorIndexOfWorkoutId);
            }
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final float _tmpAvgSpeedKmh;
            _tmpAvgSpeedKmh = _cursor.getFloat(_cursorIndexOfAvgSpeedKmh);
            final int _tmpStepCount;
            _tmpStepCount = _cursor.getInt(_cursorIndexOfStepCount);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            final String _tmpCoverPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfCoverPhotoUrl)) {
              _tmpCoverPhotoUrl = null;
            } else {
              _tmpCoverPhotoUrl = _cursor.getString(_cursorIndexOfCoverPhotoUrl);
            }
            final int _tmpXpEarned;
            _tmpXpEarned = _cursor.getInt(_cursorIndexOfXpEarned);
            final int _tmpLikesCount;
            _tmpLikesCount = _cursor.getInt(_cursorIndexOfLikesCount);
            final SurfaceType _tmpSurfaceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfSurfaceType);
            _tmpSurfaceType = Converters.INSTANCE.stringToSurfaceType(_tmp);
            final float _tmpLengthKm;
            _tmpLengthKm = _cursor.getFloat(_cursorIndexOfLengthKm);
            final long _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getLong(_cursorIndexOfDurationSeconds);
            final long _tmpStartedAt;
            _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            final long _tmpEndedAt;
            _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            final int _tmpElevationGainMeters;
            _tmpElevationGainMeters = _cursor.getInt(_cursorIndexOfElevationGainMeters);
            final List<GeoPoint> _tmpRouteCoordinates;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfRouteCoordinates);
            _tmpRouteCoordinates = Converters.INSTANCE.jsonToGeoPointList(_tmp_1);
            final boolean _tmpIsPrivate;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPrivate);
            _tmpIsPrivate = _tmp_2 != 0;
            final int _tmpDifficultyLevel;
            _tmpDifficultyLevel = _cursor.getInt(_cursorIndexOfDifficultyLevel);
            final int _tmpMudRisk;
            _tmpMudRisk = _cursor.getInt(_cursorIndexOfMudRisk);
            final int _tmpPathClarity;
            _tmpPathClarity = _cursor.getInt(_cursorIndexOfPathClarity);
            final int _tmpFatigueLevel;
            _tmpFatigueLevel = _cursor.getInt(_cursorIndexOfFatigueLevel);
            final int _tmpAnimalEncounterRisk;
            _tmpAnimalEncounterRisk = _cursor.getInt(_cursorIndexOfAnimalEncounterRisk);
            final boolean _tmpWaterAvailability;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfWaterAvailability);
            _tmpWaterAvailability = _tmp_3 != 0;
            final float _tmpAverageRating;
            _tmpAverageRating = _cursor.getFloat(_cursorIndexOfAverageRating);
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            _result = new HikeLogEntity(_tmpHikeId,_tmpCreatorFirebaseUid,_tmpCreatorUsername,_tmpCreatorProfilePictureUrl,_tmpWorkoutId,_tmpTitle,_tmpDescription,_tmpAvgSpeedKmh,_tmpStepCount,_tmpCaloriesBurned,_tmpCoverPhotoUrl,_tmpXpEarned,_tmpLikesCount,_tmpSurfaceType,_tmpLengthKm,_tmpDurationSeconds,_tmpStartedAt,_tmpEndedAt,_tmpElevationGainMeters,_tmpRouteCoordinates,_tmpIsPrivate,_tmpDifficultyLevel,_tmpMudRisk,_tmpPathClarity,_tmpFatigueLevel,_tmpAnimalEncounterRisk,_tmpWaterAvailability,_tmpAverageRating,_tmpReviewCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<HikeLogEntity>> observeByCreator(final String uid) {
    final String _sql = "SELECT * FROM hike_logs WHERE creatorFirebaseUid = ? ORDER BY endedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"hike_logs"}, new Callable<List<HikeLogEntity>>() {
      @Override
      @NonNull
      public List<HikeLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHikeId = CursorUtil.getColumnIndexOrThrow(_cursor, "hikeId");
          final int _cursorIndexOfCreatorFirebaseUid = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorFirebaseUid");
          final int _cursorIndexOfCreatorUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorUsername");
          final int _cursorIndexOfCreatorProfilePictureUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorProfilePictureUrl");
          final int _cursorIndexOfWorkoutId = CursorUtil.getColumnIndexOrThrow(_cursor, "workoutId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAvgSpeedKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "avgSpeedKmh");
          final int _cursorIndexOfStepCount = CursorUtil.getColumnIndexOrThrow(_cursor, "stepCount");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfCoverPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "coverPhotoUrl");
          final int _cursorIndexOfXpEarned = CursorUtil.getColumnIndexOrThrow(_cursor, "xpEarned");
          final int _cursorIndexOfLikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "likesCount");
          final int _cursorIndexOfSurfaceType = CursorUtil.getColumnIndexOrThrow(_cursor, "surfaceType");
          final int _cursorIndexOfLengthKm = CursorUtil.getColumnIndexOrThrow(_cursor, "lengthKm");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final int _cursorIndexOfElevationGainMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "elevationGainMeters");
          final int _cursorIndexOfRouteCoordinates = CursorUtil.getColumnIndexOrThrow(_cursor, "routeCoordinates");
          final int _cursorIndexOfIsPrivate = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrivate");
          final int _cursorIndexOfDifficultyLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "difficultyLevel");
          final int _cursorIndexOfMudRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "mudRisk");
          final int _cursorIndexOfPathClarity = CursorUtil.getColumnIndexOrThrow(_cursor, "pathClarity");
          final int _cursorIndexOfFatigueLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "fatigueLevel");
          final int _cursorIndexOfAnimalEncounterRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "animalEncounterRisk");
          final int _cursorIndexOfWaterAvailability = CursorUtil.getColumnIndexOrThrow(_cursor, "waterAvailability");
          final int _cursorIndexOfAverageRating = CursorUtil.getColumnIndexOrThrow(_cursor, "averageRating");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final List<HikeLogEntity> _result = new ArrayList<HikeLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HikeLogEntity _item;
            final String _tmpHikeId;
            _tmpHikeId = _cursor.getString(_cursorIndexOfHikeId);
            final String _tmpCreatorFirebaseUid;
            _tmpCreatorFirebaseUid = _cursor.getString(_cursorIndexOfCreatorFirebaseUid);
            final String _tmpCreatorUsername;
            _tmpCreatorUsername = _cursor.getString(_cursorIndexOfCreatorUsername);
            final String _tmpCreatorProfilePictureUrl;
            if (_cursor.isNull(_cursorIndexOfCreatorProfilePictureUrl)) {
              _tmpCreatorProfilePictureUrl = null;
            } else {
              _tmpCreatorProfilePictureUrl = _cursor.getString(_cursorIndexOfCreatorProfilePictureUrl);
            }
            final String _tmpWorkoutId;
            if (_cursor.isNull(_cursorIndexOfWorkoutId)) {
              _tmpWorkoutId = null;
            } else {
              _tmpWorkoutId = _cursor.getString(_cursorIndexOfWorkoutId);
            }
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final float _tmpAvgSpeedKmh;
            _tmpAvgSpeedKmh = _cursor.getFloat(_cursorIndexOfAvgSpeedKmh);
            final int _tmpStepCount;
            _tmpStepCount = _cursor.getInt(_cursorIndexOfStepCount);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            final String _tmpCoverPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfCoverPhotoUrl)) {
              _tmpCoverPhotoUrl = null;
            } else {
              _tmpCoverPhotoUrl = _cursor.getString(_cursorIndexOfCoverPhotoUrl);
            }
            final int _tmpXpEarned;
            _tmpXpEarned = _cursor.getInt(_cursorIndexOfXpEarned);
            final int _tmpLikesCount;
            _tmpLikesCount = _cursor.getInt(_cursorIndexOfLikesCount);
            final SurfaceType _tmpSurfaceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfSurfaceType);
            _tmpSurfaceType = Converters.INSTANCE.stringToSurfaceType(_tmp);
            final float _tmpLengthKm;
            _tmpLengthKm = _cursor.getFloat(_cursorIndexOfLengthKm);
            final long _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getLong(_cursorIndexOfDurationSeconds);
            final long _tmpStartedAt;
            _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            final long _tmpEndedAt;
            _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            final int _tmpElevationGainMeters;
            _tmpElevationGainMeters = _cursor.getInt(_cursorIndexOfElevationGainMeters);
            final List<GeoPoint> _tmpRouteCoordinates;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfRouteCoordinates);
            _tmpRouteCoordinates = Converters.INSTANCE.jsonToGeoPointList(_tmp_1);
            final boolean _tmpIsPrivate;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPrivate);
            _tmpIsPrivate = _tmp_2 != 0;
            final int _tmpDifficultyLevel;
            _tmpDifficultyLevel = _cursor.getInt(_cursorIndexOfDifficultyLevel);
            final int _tmpMudRisk;
            _tmpMudRisk = _cursor.getInt(_cursorIndexOfMudRisk);
            final int _tmpPathClarity;
            _tmpPathClarity = _cursor.getInt(_cursorIndexOfPathClarity);
            final int _tmpFatigueLevel;
            _tmpFatigueLevel = _cursor.getInt(_cursorIndexOfFatigueLevel);
            final int _tmpAnimalEncounterRisk;
            _tmpAnimalEncounterRisk = _cursor.getInt(_cursorIndexOfAnimalEncounterRisk);
            final boolean _tmpWaterAvailability;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfWaterAvailability);
            _tmpWaterAvailability = _tmp_3 != 0;
            final float _tmpAverageRating;
            _tmpAverageRating = _cursor.getFloat(_cursorIndexOfAverageRating);
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            _item = new HikeLogEntity(_tmpHikeId,_tmpCreatorFirebaseUid,_tmpCreatorUsername,_tmpCreatorProfilePictureUrl,_tmpWorkoutId,_tmpTitle,_tmpDescription,_tmpAvgSpeedKmh,_tmpStepCount,_tmpCaloriesBurned,_tmpCoverPhotoUrl,_tmpXpEarned,_tmpLikesCount,_tmpSurfaceType,_tmpLengthKm,_tmpDurationSeconds,_tmpStartedAt,_tmpEndedAt,_tmpElevationGainMeters,_tmpRouteCoordinates,_tmpIsPrivate,_tmpDifficultyLevel,_tmpMudRisk,_tmpPathClarity,_tmpFatigueLevel,_tmpAnimalEncounterRisk,_tmpWaterAvailability,_tmpAverageRating,_tmpReviewCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getByCreator(final String uid,
      final Continuation<? super List<HikeLogEntity>> $completion) {
    final String _sql = "SELECT * FROM hike_logs WHERE creatorFirebaseUid = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<HikeLogEntity>>() {
      @Override
      @NonNull
      public List<HikeLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHikeId = CursorUtil.getColumnIndexOrThrow(_cursor, "hikeId");
          final int _cursorIndexOfCreatorFirebaseUid = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorFirebaseUid");
          final int _cursorIndexOfCreatorUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorUsername");
          final int _cursorIndexOfCreatorProfilePictureUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorProfilePictureUrl");
          final int _cursorIndexOfWorkoutId = CursorUtil.getColumnIndexOrThrow(_cursor, "workoutId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAvgSpeedKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "avgSpeedKmh");
          final int _cursorIndexOfStepCount = CursorUtil.getColumnIndexOrThrow(_cursor, "stepCount");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfCoverPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "coverPhotoUrl");
          final int _cursorIndexOfXpEarned = CursorUtil.getColumnIndexOrThrow(_cursor, "xpEarned");
          final int _cursorIndexOfLikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "likesCount");
          final int _cursorIndexOfSurfaceType = CursorUtil.getColumnIndexOrThrow(_cursor, "surfaceType");
          final int _cursorIndexOfLengthKm = CursorUtil.getColumnIndexOrThrow(_cursor, "lengthKm");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final int _cursorIndexOfElevationGainMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "elevationGainMeters");
          final int _cursorIndexOfRouteCoordinates = CursorUtil.getColumnIndexOrThrow(_cursor, "routeCoordinates");
          final int _cursorIndexOfIsPrivate = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrivate");
          final int _cursorIndexOfDifficultyLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "difficultyLevel");
          final int _cursorIndexOfMudRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "mudRisk");
          final int _cursorIndexOfPathClarity = CursorUtil.getColumnIndexOrThrow(_cursor, "pathClarity");
          final int _cursorIndexOfFatigueLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "fatigueLevel");
          final int _cursorIndexOfAnimalEncounterRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "animalEncounterRisk");
          final int _cursorIndexOfWaterAvailability = CursorUtil.getColumnIndexOrThrow(_cursor, "waterAvailability");
          final int _cursorIndexOfAverageRating = CursorUtil.getColumnIndexOrThrow(_cursor, "averageRating");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final List<HikeLogEntity> _result = new ArrayList<HikeLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HikeLogEntity _item;
            final String _tmpHikeId;
            _tmpHikeId = _cursor.getString(_cursorIndexOfHikeId);
            final String _tmpCreatorFirebaseUid;
            _tmpCreatorFirebaseUid = _cursor.getString(_cursorIndexOfCreatorFirebaseUid);
            final String _tmpCreatorUsername;
            _tmpCreatorUsername = _cursor.getString(_cursorIndexOfCreatorUsername);
            final String _tmpCreatorProfilePictureUrl;
            if (_cursor.isNull(_cursorIndexOfCreatorProfilePictureUrl)) {
              _tmpCreatorProfilePictureUrl = null;
            } else {
              _tmpCreatorProfilePictureUrl = _cursor.getString(_cursorIndexOfCreatorProfilePictureUrl);
            }
            final String _tmpWorkoutId;
            if (_cursor.isNull(_cursorIndexOfWorkoutId)) {
              _tmpWorkoutId = null;
            } else {
              _tmpWorkoutId = _cursor.getString(_cursorIndexOfWorkoutId);
            }
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final float _tmpAvgSpeedKmh;
            _tmpAvgSpeedKmh = _cursor.getFloat(_cursorIndexOfAvgSpeedKmh);
            final int _tmpStepCount;
            _tmpStepCount = _cursor.getInt(_cursorIndexOfStepCount);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            final String _tmpCoverPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfCoverPhotoUrl)) {
              _tmpCoverPhotoUrl = null;
            } else {
              _tmpCoverPhotoUrl = _cursor.getString(_cursorIndexOfCoverPhotoUrl);
            }
            final int _tmpXpEarned;
            _tmpXpEarned = _cursor.getInt(_cursorIndexOfXpEarned);
            final int _tmpLikesCount;
            _tmpLikesCount = _cursor.getInt(_cursorIndexOfLikesCount);
            final SurfaceType _tmpSurfaceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfSurfaceType);
            _tmpSurfaceType = Converters.INSTANCE.stringToSurfaceType(_tmp);
            final float _tmpLengthKm;
            _tmpLengthKm = _cursor.getFloat(_cursorIndexOfLengthKm);
            final long _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getLong(_cursorIndexOfDurationSeconds);
            final long _tmpStartedAt;
            _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            final long _tmpEndedAt;
            _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            final int _tmpElevationGainMeters;
            _tmpElevationGainMeters = _cursor.getInt(_cursorIndexOfElevationGainMeters);
            final List<GeoPoint> _tmpRouteCoordinates;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfRouteCoordinates);
            _tmpRouteCoordinates = Converters.INSTANCE.jsonToGeoPointList(_tmp_1);
            final boolean _tmpIsPrivate;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPrivate);
            _tmpIsPrivate = _tmp_2 != 0;
            final int _tmpDifficultyLevel;
            _tmpDifficultyLevel = _cursor.getInt(_cursorIndexOfDifficultyLevel);
            final int _tmpMudRisk;
            _tmpMudRisk = _cursor.getInt(_cursorIndexOfMudRisk);
            final int _tmpPathClarity;
            _tmpPathClarity = _cursor.getInt(_cursorIndexOfPathClarity);
            final int _tmpFatigueLevel;
            _tmpFatigueLevel = _cursor.getInt(_cursorIndexOfFatigueLevel);
            final int _tmpAnimalEncounterRisk;
            _tmpAnimalEncounterRisk = _cursor.getInt(_cursorIndexOfAnimalEncounterRisk);
            final boolean _tmpWaterAvailability;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfWaterAvailability);
            _tmpWaterAvailability = _tmp_3 != 0;
            final float _tmpAverageRating;
            _tmpAverageRating = _cursor.getFloat(_cursorIndexOfAverageRating);
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            _item = new HikeLogEntity(_tmpHikeId,_tmpCreatorFirebaseUid,_tmpCreatorUsername,_tmpCreatorProfilePictureUrl,_tmpWorkoutId,_tmpTitle,_tmpDescription,_tmpAvgSpeedKmh,_tmpStepCount,_tmpCaloriesBurned,_tmpCoverPhotoUrl,_tmpXpEarned,_tmpLikesCount,_tmpSurfaceType,_tmpLengthKm,_tmpDurationSeconds,_tmpStartedAt,_tmpEndedAt,_tmpElevationGainMeters,_tmpRouteCoordinates,_tmpIsPrivate,_tmpDifficultyLevel,_tmpMudRisk,_tmpPathClarity,_tmpFatigueLevel,_tmpAnimalEncounterRisk,_tmpWaterAvailability,_tmpAverageRating,_tmpReviewCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<HikeLogEntity>> observeLikedHikes(final String uid) {
    final String _sql = "\n"
            + "        SELECT h.* FROM hike_logs h\n"
            + "         INNER JOIN likes l ON l.hikeId = h.hikeId\n"
            + "         WHERE l.userUid = ?\n"
            + "         ORDER BY l.createdAt DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"hike_logs",
        "likes"}, new Callable<List<HikeLogEntity>>() {
      @Override
      @NonNull
      public List<HikeLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHikeId = CursorUtil.getColumnIndexOrThrow(_cursor, "hikeId");
          final int _cursorIndexOfCreatorFirebaseUid = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorFirebaseUid");
          final int _cursorIndexOfCreatorUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorUsername");
          final int _cursorIndexOfCreatorProfilePictureUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorProfilePictureUrl");
          final int _cursorIndexOfWorkoutId = CursorUtil.getColumnIndexOrThrow(_cursor, "workoutId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAvgSpeedKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "avgSpeedKmh");
          final int _cursorIndexOfStepCount = CursorUtil.getColumnIndexOrThrow(_cursor, "stepCount");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfCoverPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "coverPhotoUrl");
          final int _cursorIndexOfXpEarned = CursorUtil.getColumnIndexOrThrow(_cursor, "xpEarned");
          final int _cursorIndexOfLikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "likesCount");
          final int _cursorIndexOfSurfaceType = CursorUtil.getColumnIndexOrThrow(_cursor, "surfaceType");
          final int _cursorIndexOfLengthKm = CursorUtil.getColumnIndexOrThrow(_cursor, "lengthKm");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final int _cursorIndexOfElevationGainMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "elevationGainMeters");
          final int _cursorIndexOfRouteCoordinates = CursorUtil.getColumnIndexOrThrow(_cursor, "routeCoordinates");
          final int _cursorIndexOfIsPrivate = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrivate");
          final int _cursorIndexOfDifficultyLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "difficultyLevel");
          final int _cursorIndexOfMudRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "mudRisk");
          final int _cursorIndexOfPathClarity = CursorUtil.getColumnIndexOrThrow(_cursor, "pathClarity");
          final int _cursorIndexOfFatigueLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "fatigueLevel");
          final int _cursorIndexOfAnimalEncounterRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "animalEncounterRisk");
          final int _cursorIndexOfWaterAvailability = CursorUtil.getColumnIndexOrThrow(_cursor, "waterAvailability");
          final int _cursorIndexOfAverageRating = CursorUtil.getColumnIndexOrThrow(_cursor, "averageRating");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final List<HikeLogEntity> _result = new ArrayList<HikeLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HikeLogEntity _item;
            final String _tmpHikeId;
            _tmpHikeId = _cursor.getString(_cursorIndexOfHikeId);
            final String _tmpCreatorFirebaseUid;
            _tmpCreatorFirebaseUid = _cursor.getString(_cursorIndexOfCreatorFirebaseUid);
            final String _tmpCreatorUsername;
            _tmpCreatorUsername = _cursor.getString(_cursorIndexOfCreatorUsername);
            final String _tmpCreatorProfilePictureUrl;
            if (_cursor.isNull(_cursorIndexOfCreatorProfilePictureUrl)) {
              _tmpCreatorProfilePictureUrl = null;
            } else {
              _tmpCreatorProfilePictureUrl = _cursor.getString(_cursorIndexOfCreatorProfilePictureUrl);
            }
            final String _tmpWorkoutId;
            if (_cursor.isNull(_cursorIndexOfWorkoutId)) {
              _tmpWorkoutId = null;
            } else {
              _tmpWorkoutId = _cursor.getString(_cursorIndexOfWorkoutId);
            }
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final float _tmpAvgSpeedKmh;
            _tmpAvgSpeedKmh = _cursor.getFloat(_cursorIndexOfAvgSpeedKmh);
            final int _tmpStepCount;
            _tmpStepCount = _cursor.getInt(_cursorIndexOfStepCount);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            final String _tmpCoverPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfCoverPhotoUrl)) {
              _tmpCoverPhotoUrl = null;
            } else {
              _tmpCoverPhotoUrl = _cursor.getString(_cursorIndexOfCoverPhotoUrl);
            }
            final int _tmpXpEarned;
            _tmpXpEarned = _cursor.getInt(_cursorIndexOfXpEarned);
            final int _tmpLikesCount;
            _tmpLikesCount = _cursor.getInt(_cursorIndexOfLikesCount);
            final SurfaceType _tmpSurfaceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfSurfaceType);
            _tmpSurfaceType = Converters.INSTANCE.stringToSurfaceType(_tmp);
            final float _tmpLengthKm;
            _tmpLengthKm = _cursor.getFloat(_cursorIndexOfLengthKm);
            final long _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getLong(_cursorIndexOfDurationSeconds);
            final long _tmpStartedAt;
            _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            final long _tmpEndedAt;
            _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            final int _tmpElevationGainMeters;
            _tmpElevationGainMeters = _cursor.getInt(_cursorIndexOfElevationGainMeters);
            final List<GeoPoint> _tmpRouteCoordinates;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfRouteCoordinates);
            _tmpRouteCoordinates = Converters.INSTANCE.jsonToGeoPointList(_tmp_1);
            final boolean _tmpIsPrivate;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPrivate);
            _tmpIsPrivate = _tmp_2 != 0;
            final int _tmpDifficultyLevel;
            _tmpDifficultyLevel = _cursor.getInt(_cursorIndexOfDifficultyLevel);
            final int _tmpMudRisk;
            _tmpMudRisk = _cursor.getInt(_cursorIndexOfMudRisk);
            final int _tmpPathClarity;
            _tmpPathClarity = _cursor.getInt(_cursorIndexOfPathClarity);
            final int _tmpFatigueLevel;
            _tmpFatigueLevel = _cursor.getInt(_cursorIndexOfFatigueLevel);
            final int _tmpAnimalEncounterRisk;
            _tmpAnimalEncounterRisk = _cursor.getInt(_cursorIndexOfAnimalEncounterRisk);
            final boolean _tmpWaterAvailability;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfWaterAvailability);
            _tmpWaterAvailability = _tmp_3 != 0;
            final float _tmpAverageRating;
            _tmpAverageRating = _cursor.getFloat(_cursorIndexOfAverageRating);
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            _item = new HikeLogEntity(_tmpHikeId,_tmpCreatorFirebaseUid,_tmpCreatorUsername,_tmpCreatorProfilePictureUrl,_tmpWorkoutId,_tmpTitle,_tmpDescription,_tmpAvgSpeedKmh,_tmpStepCount,_tmpCaloriesBurned,_tmpCoverPhotoUrl,_tmpXpEarned,_tmpLikesCount,_tmpSurfaceType,_tmpLengthKm,_tmpDurationSeconds,_tmpStartedAt,_tmpEndedAt,_tmpElevationGainMeters,_tmpRouteCoordinates,_tmpIsPrivate,_tmpDifficultyLevel,_tmpMudRisk,_tmpPathClarity,_tmpFatigueLevel,_tmpAnimalEncounterRisk,_tmpWaterAvailability,_tmpAverageRating,_tmpReviewCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<HikeLogEntity>> observePublicFeed(final int limit) {
    final String _sql = "SELECT * FROM hike_logs WHERE isPrivate = 0 ORDER BY endedAt DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"hike_logs"}, new Callable<List<HikeLogEntity>>() {
      @Override
      @NonNull
      public List<HikeLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHikeId = CursorUtil.getColumnIndexOrThrow(_cursor, "hikeId");
          final int _cursorIndexOfCreatorFirebaseUid = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorFirebaseUid");
          final int _cursorIndexOfCreatorUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorUsername");
          final int _cursorIndexOfCreatorProfilePictureUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorProfilePictureUrl");
          final int _cursorIndexOfWorkoutId = CursorUtil.getColumnIndexOrThrow(_cursor, "workoutId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAvgSpeedKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "avgSpeedKmh");
          final int _cursorIndexOfStepCount = CursorUtil.getColumnIndexOrThrow(_cursor, "stepCount");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfCoverPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "coverPhotoUrl");
          final int _cursorIndexOfXpEarned = CursorUtil.getColumnIndexOrThrow(_cursor, "xpEarned");
          final int _cursorIndexOfLikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "likesCount");
          final int _cursorIndexOfSurfaceType = CursorUtil.getColumnIndexOrThrow(_cursor, "surfaceType");
          final int _cursorIndexOfLengthKm = CursorUtil.getColumnIndexOrThrow(_cursor, "lengthKm");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final int _cursorIndexOfElevationGainMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "elevationGainMeters");
          final int _cursorIndexOfRouteCoordinates = CursorUtil.getColumnIndexOrThrow(_cursor, "routeCoordinates");
          final int _cursorIndexOfIsPrivate = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrivate");
          final int _cursorIndexOfDifficultyLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "difficultyLevel");
          final int _cursorIndexOfMudRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "mudRisk");
          final int _cursorIndexOfPathClarity = CursorUtil.getColumnIndexOrThrow(_cursor, "pathClarity");
          final int _cursorIndexOfFatigueLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "fatigueLevel");
          final int _cursorIndexOfAnimalEncounterRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "animalEncounterRisk");
          final int _cursorIndexOfWaterAvailability = CursorUtil.getColumnIndexOrThrow(_cursor, "waterAvailability");
          final int _cursorIndexOfAverageRating = CursorUtil.getColumnIndexOrThrow(_cursor, "averageRating");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final List<HikeLogEntity> _result = new ArrayList<HikeLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HikeLogEntity _item;
            final String _tmpHikeId;
            _tmpHikeId = _cursor.getString(_cursorIndexOfHikeId);
            final String _tmpCreatorFirebaseUid;
            _tmpCreatorFirebaseUid = _cursor.getString(_cursorIndexOfCreatorFirebaseUid);
            final String _tmpCreatorUsername;
            _tmpCreatorUsername = _cursor.getString(_cursorIndexOfCreatorUsername);
            final String _tmpCreatorProfilePictureUrl;
            if (_cursor.isNull(_cursorIndexOfCreatorProfilePictureUrl)) {
              _tmpCreatorProfilePictureUrl = null;
            } else {
              _tmpCreatorProfilePictureUrl = _cursor.getString(_cursorIndexOfCreatorProfilePictureUrl);
            }
            final String _tmpWorkoutId;
            if (_cursor.isNull(_cursorIndexOfWorkoutId)) {
              _tmpWorkoutId = null;
            } else {
              _tmpWorkoutId = _cursor.getString(_cursorIndexOfWorkoutId);
            }
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final float _tmpAvgSpeedKmh;
            _tmpAvgSpeedKmh = _cursor.getFloat(_cursorIndexOfAvgSpeedKmh);
            final int _tmpStepCount;
            _tmpStepCount = _cursor.getInt(_cursorIndexOfStepCount);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            final String _tmpCoverPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfCoverPhotoUrl)) {
              _tmpCoverPhotoUrl = null;
            } else {
              _tmpCoverPhotoUrl = _cursor.getString(_cursorIndexOfCoverPhotoUrl);
            }
            final int _tmpXpEarned;
            _tmpXpEarned = _cursor.getInt(_cursorIndexOfXpEarned);
            final int _tmpLikesCount;
            _tmpLikesCount = _cursor.getInt(_cursorIndexOfLikesCount);
            final SurfaceType _tmpSurfaceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfSurfaceType);
            _tmpSurfaceType = Converters.INSTANCE.stringToSurfaceType(_tmp);
            final float _tmpLengthKm;
            _tmpLengthKm = _cursor.getFloat(_cursorIndexOfLengthKm);
            final long _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getLong(_cursorIndexOfDurationSeconds);
            final long _tmpStartedAt;
            _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            final long _tmpEndedAt;
            _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            final int _tmpElevationGainMeters;
            _tmpElevationGainMeters = _cursor.getInt(_cursorIndexOfElevationGainMeters);
            final List<GeoPoint> _tmpRouteCoordinates;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfRouteCoordinates);
            _tmpRouteCoordinates = Converters.INSTANCE.jsonToGeoPointList(_tmp_1);
            final boolean _tmpIsPrivate;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPrivate);
            _tmpIsPrivate = _tmp_2 != 0;
            final int _tmpDifficultyLevel;
            _tmpDifficultyLevel = _cursor.getInt(_cursorIndexOfDifficultyLevel);
            final int _tmpMudRisk;
            _tmpMudRisk = _cursor.getInt(_cursorIndexOfMudRisk);
            final int _tmpPathClarity;
            _tmpPathClarity = _cursor.getInt(_cursorIndexOfPathClarity);
            final int _tmpFatigueLevel;
            _tmpFatigueLevel = _cursor.getInt(_cursorIndexOfFatigueLevel);
            final int _tmpAnimalEncounterRisk;
            _tmpAnimalEncounterRisk = _cursor.getInt(_cursorIndexOfAnimalEncounterRisk);
            final boolean _tmpWaterAvailability;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfWaterAvailability);
            _tmpWaterAvailability = _tmp_3 != 0;
            final float _tmpAverageRating;
            _tmpAverageRating = _cursor.getFloat(_cursorIndexOfAverageRating);
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            _item = new HikeLogEntity(_tmpHikeId,_tmpCreatorFirebaseUid,_tmpCreatorUsername,_tmpCreatorProfilePictureUrl,_tmpWorkoutId,_tmpTitle,_tmpDescription,_tmpAvgSpeedKmh,_tmpStepCount,_tmpCaloriesBurned,_tmpCoverPhotoUrl,_tmpXpEarned,_tmpLikesCount,_tmpSurfaceType,_tmpLengthKm,_tmpDurationSeconds,_tmpStartedAt,_tmpEndedAt,_tmpElevationGainMeters,_tmpRouteCoordinates,_tmpIsPrivate,_tmpDifficultyLevel,_tmpMudRisk,_tmpPathClarity,_tmpFatigueLevel,_tmpAnimalEncounterRisk,_tmpWaterAvailability,_tmpAverageRating,_tmpReviewCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object search(final String q,
      final Continuation<? super List<HikeLogEntity>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM hike_logs\n"
            + "         WHERE isPrivate = 0\n"
            + "           AND (title LIKE '%' || ? || '%' OR description LIKE '%' || ? || '%')\n"
            + "         ORDER BY likesCount DESC\n"
            + "         LIMIT 100\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, q);
    _argIndex = 2;
    _statement.bindString(_argIndex, q);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<HikeLogEntity>>() {
      @Override
      @NonNull
      public List<HikeLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHikeId = CursorUtil.getColumnIndexOrThrow(_cursor, "hikeId");
          final int _cursorIndexOfCreatorFirebaseUid = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorFirebaseUid");
          final int _cursorIndexOfCreatorUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorUsername");
          final int _cursorIndexOfCreatorProfilePictureUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "creatorProfilePictureUrl");
          final int _cursorIndexOfWorkoutId = CursorUtil.getColumnIndexOrThrow(_cursor, "workoutId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfAvgSpeedKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "avgSpeedKmh");
          final int _cursorIndexOfStepCount = CursorUtil.getColumnIndexOrThrow(_cursor, "stepCount");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfCoverPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "coverPhotoUrl");
          final int _cursorIndexOfXpEarned = CursorUtil.getColumnIndexOrThrow(_cursor, "xpEarned");
          final int _cursorIndexOfLikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "likesCount");
          final int _cursorIndexOfSurfaceType = CursorUtil.getColumnIndexOrThrow(_cursor, "surfaceType");
          final int _cursorIndexOfLengthKm = CursorUtil.getColumnIndexOrThrow(_cursor, "lengthKm");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final int _cursorIndexOfEndedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "endedAt");
          final int _cursorIndexOfElevationGainMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "elevationGainMeters");
          final int _cursorIndexOfRouteCoordinates = CursorUtil.getColumnIndexOrThrow(_cursor, "routeCoordinates");
          final int _cursorIndexOfIsPrivate = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrivate");
          final int _cursorIndexOfDifficultyLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "difficultyLevel");
          final int _cursorIndexOfMudRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "mudRisk");
          final int _cursorIndexOfPathClarity = CursorUtil.getColumnIndexOrThrow(_cursor, "pathClarity");
          final int _cursorIndexOfFatigueLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "fatigueLevel");
          final int _cursorIndexOfAnimalEncounterRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "animalEncounterRisk");
          final int _cursorIndexOfWaterAvailability = CursorUtil.getColumnIndexOrThrow(_cursor, "waterAvailability");
          final int _cursorIndexOfAverageRating = CursorUtil.getColumnIndexOrThrow(_cursor, "averageRating");
          final int _cursorIndexOfReviewCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewCount");
          final List<HikeLogEntity> _result = new ArrayList<HikeLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HikeLogEntity _item;
            final String _tmpHikeId;
            _tmpHikeId = _cursor.getString(_cursorIndexOfHikeId);
            final String _tmpCreatorFirebaseUid;
            _tmpCreatorFirebaseUid = _cursor.getString(_cursorIndexOfCreatorFirebaseUid);
            final String _tmpCreatorUsername;
            _tmpCreatorUsername = _cursor.getString(_cursorIndexOfCreatorUsername);
            final String _tmpCreatorProfilePictureUrl;
            if (_cursor.isNull(_cursorIndexOfCreatorProfilePictureUrl)) {
              _tmpCreatorProfilePictureUrl = null;
            } else {
              _tmpCreatorProfilePictureUrl = _cursor.getString(_cursorIndexOfCreatorProfilePictureUrl);
            }
            final String _tmpWorkoutId;
            if (_cursor.isNull(_cursorIndexOfWorkoutId)) {
              _tmpWorkoutId = null;
            } else {
              _tmpWorkoutId = _cursor.getString(_cursorIndexOfWorkoutId);
            }
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final float _tmpAvgSpeedKmh;
            _tmpAvgSpeedKmh = _cursor.getFloat(_cursorIndexOfAvgSpeedKmh);
            final int _tmpStepCount;
            _tmpStepCount = _cursor.getInt(_cursorIndexOfStepCount);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            final String _tmpCoverPhotoUrl;
            if (_cursor.isNull(_cursorIndexOfCoverPhotoUrl)) {
              _tmpCoverPhotoUrl = null;
            } else {
              _tmpCoverPhotoUrl = _cursor.getString(_cursorIndexOfCoverPhotoUrl);
            }
            final int _tmpXpEarned;
            _tmpXpEarned = _cursor.getInt(_cursorIndexOfXpEarned);
            final int _tmpLikesCount;
            _tmpLikesCount = _cursor.getInt(_cursorIndexOfLikesCount);
            final SurfaceType _tmpSurfaceType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfSurfaceType);
            _tmpSurfaceType = Converters.INSTANCE.stringToSurfaceType(_tmp);
            final float _tmpLengthKm;
            _tmpLengthKm = _cursor.getFloat(_cursorIndexOfLengthKm);
            final long _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getLong(_cursorIndexOfDurationSeconds);
            final long _tmpStartedAt;
            _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            final long _tmpEndedAt;
            _tmpEndedAt = _cursor.getLong(_cursorIndexOfEndedAt);
            final int _tmpElevationGainMeters;
            _tmpElevationGainMeters = _cursor.getInt(_cursorIndexOfElevationGainMeters);
            final List<GeoPoint> _tmpRouteCoordinates;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfRouteCoordinates);
            _tmpRouteCoordinates = Converters.INSTANCE.jsonToGeoPointList(_tmp_1);
            final boolean _tmpIsPrivate;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPrivate);
            _tmpIsPrivate = _tmp_2 != 0;
            final int _tmpDifficultyLevel;
            _tmpDifficultyLevel = _cursor.getInt(_cursorIndexOfDifficultyLevel);
            final int _tmpMudRisk;
            _tmpMudRisk = _cursor.getInt(_cursorIndexOfMudRisk);
            final int _tmpPathClarity;
            _tmpPathClarity = _cursor.getInt(_cursorIndexOfPathClarity);
            final int _tmpFatigueLevel;
            _tmpFatigueLevel = _cursor.getInt(_cursorIndexOfFatigueLevel);
            final int _tmpAnimalEncounterRisk;
            _tmpAnimalEncounterRisk = _cursor.getInt(_cursorIndexOfAnimalEncounterRisk);
            final boolean _tmpWaterAvailability;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfWaterAvailability);
            _tmpWaterAvailability = _tmp_3 != 0;
            final float _tmpAverageRating;
            _tmpAverageRating = _cursor.getFloat(_cursorIndexOfAverageRating);
            final int _tmpReviewCount;
            _tmpReviewCount = _cursor.getInt(_cursorIndexOfReviewCount);
            _item = new HikeLogEntity(_tmpHikeId,_tmpCreatorFirebaseUid,_tmpCreatorUsername,_tmpCreatorProfilePictureUrl,_tmpWorkoutId,_tmpTitle,_tmpDescription,_tmpAvgSpeedKmh,_tmpStepCount,_tmpCaloriesBurned,_tmpCoverPhotoUrl,_tmpXpEarned,_tmpLikesCount,_tmpSurfaceType,_tmpLengthKm,_tmpDurationSeconds,_tmpStartedAt,_tmpEndedAt,_tmpElevationGainMeters,_tmpRouteCoordinates,_tmpIsPrivate,_tmpDifficultyLevel,_tmpMudRisk,_tmpPathClarity,_tmpFatigueLevel,_tmpAnimalEncounterRisk,_tmpWaterAvailability,_tmpAverageRating,_tmpReviewCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
