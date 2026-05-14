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
import com.wildtrail.app.data.local.entity.TrailReviewEntity;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
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
public final class TrailReviewDao_Impl implements TrailReviewDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TrailReviewEntity> __insertionAdapterOfTrailReviewEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public TrailReviewDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTrailReviewEntity = new EntityInsertionAdapter<TrailReviewEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `trail_reviews` (`reviewId`,`reviewerUid`,`hikeId`,`overallRating`,`fatigueLevel`,`pathClarity`,`difficultyLevel`,`mudRisk`,`animalEncounterRisk`,`waterAvailability`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TrailReviewEntity entity) {
        statement.bindString(1, entity.getReviewId());
        statement.bindString(2, entity.getReviewerUid());
        statement.bindString(3, entity.getHikeId());
        statement.bindLong(4, entity.getOverallRating());
        statement.bindLong(5, entity.getFatigueLevel());
        statement.bindLong(6, entity.getPathClarity());
        statement.bindLong(7, entity.getDifficultyLevel());
        statement.bindLong(8, entity.getMudRisk());
        statement.bindLong(9, entity.getAnimalEncounterRisk());
        final int _tmp = entity.getWaterAvailability() ? 1 : 0;
        statement.bindLong(10, _tmp);
        statement.bindLong(11, entity.getCreatedAt());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM trail_reviews WHERE reviewId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final TrailReviewEntity review,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTrailReviewEntity.insert(review);
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
  public Flow<List<TrailReviewEntity>> observeForHike(final String hikeId) {
    final String _sql = "SELECT * FROM trail_reviews WHERE hikeId = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, hikeId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trail_reviews"}, new Callable<List<TrailReviewEntity>>() {
      @Override
      @NonNull
      public List<TrailReviewEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfReviewId = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewId");
          final int _cursorIndexOfReviewerUid = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewerUid");
          final int _cursorIndexOfHikeId = CursorUtil.getColumnIndexOrThrow(_cursor, "hikeId");
          final int _cursorIndexOfOverallRating = CursorUtil.getColumnIndexOrThrow(_cursor, "overallRating");
          final int _cursorIndexOfFatigueLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "fatigueLevel");
          final int _cursorIndexOfPathClarity = CursorUtil.getColumnIndexOrThrow(_cursor, "pathClarity");
          final int _cursorIndexOfDifficultyLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "difficultyLevel");
          final int _cursorIndexOfMudRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "mudRisk");
          final int _cursorIndexOfAnimalEncounterRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "animalEncounterRisk");
          final int _cursorIndexOfWaterAvailability = CursorUtil.getColumnIndexOrThrow(_cursor, "waterAvailability");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<TrailReviewEntity> _result = new ArrayList<TrailReviewEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrailReviewEntity _item;
            final String _tmpReviewId;
            _tmpReviewId = _cursor.getString(_cursorIndexOfReviewId);
            final String _tmpReviewerUid;
            _tmpReviewerUid = _cursor.getString(_cursorIndexOfReviewerUid);
            final String _tmpHikeId;
            _tmpHikeId = _cursor.getString(_cursorIndexOfHikeId);
            final int _tmpOverallRating;
            _tmpOverallRating = _cursor.getInt(_cursorIndexOfOverallRating);
            final int _tmpFatigueLevel;
            _tmpFatigueLevel = _cursor.getInt(_cursorIndexOfFatigueLevel);
            final int _tmpPathClarity;
            _tmpPathClarity = _cursor.getInt(_cursorIndexOfPathClarity);
            final int _tmpDifficultyLevel;
            _tmpDifficultyLevel = _cursor.getInt(_cursorIndexOfDifficultyLevel);
            final int _tmpMudRisk;
            _tmpMudRisk = _cursor.getInt(_cursorIndexOfMudRisk);
            final int _tmpAnimalEncounterRisk;
            _tmpAnimalEncounterRisk = _cursor.getInt(_cursorIndexOfAnimalEncounterRisk);
            final boolean _tmpWaterAvailability;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfWaterAvailability);
            _tmpWaterAvailability = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new TrailReviewEntity(_tmpReviewId,_tmpReviewerUid,_tmpHikeId,_tmpOverallRating,_tmpFatigueLevel,_tmpPathClarity,_tmpDifficultyLevel,_tmpMudRisk,_tmpAnimalEncounterRisk,_tmpWaterAvailability,_tmpCreatedAt);
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
  public Object getMine(final String uid, final String hikeId,
      final Continuation<? super TrailReviewEntity> $completion) {
    final String _sql = "SELECT * FROM trail_reviews WHERE reviewerUid = ? AND hikeId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    _argIndex = 2;
    _statement.bindString(_argIndex, hikeId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TrailReviewEntity>() {
      @Override
      @Nullable
      public TrailReviewEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfReviewId = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewId");
          final int _cursorIndexOfReviewerUid = CursorUtil.getColumnIndexOrThrow(_cursor, "reviewerUid");
          final int _cursorIndexOfHikeId = CursorUtil.getColumnIndexOrThrow(_cursor, "hikeId");
          final int _cursorIndexOfOverallRating = CursorUtil.getColumnIndexOrThrow(_cursor, "overallRating");
          final int _cursorIndexOfFatigueLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "fatigueLevel");
          final int _cursorIndexOfPathClarity = CursorUtil.getColumnIndexOrThrow(_cursor, "pathClarity");
          final int _cursorIndexOfDifficultyLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "difficultyLevel");
          final int _cursorIndexOfMudRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "mudRisk");
          final int _cursorIndexOfAnimalEncounterRisk = CursorUtil.getColumnIndexOrThrow(_cursor, "animalEncounterRisk");
          final int _cursorIndexOfWaterAvailability = CursorUtil.getColumnIndexOrThrow(_cursor, "waterAvailability");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final TrailReviewEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpReviewId;
            _tmpReviewId = _cursor.getString(_cursorIndexOfReviewId);
            final String _tmpReviewerUid;
            _tmpReviewerUid = _cursor.getString(_cursorIndexOfReviewerUid);
            final String _tmpHikeId;
            _tmpHikeId = _cursor.getString(_cursorIndexOfHikeId);
            final int _tmpOverallRating;
            _tmpOverallRating = _cursor.getInt(_cursorIndexOfOverallRating);
            final int _tmpFatigueLevel;
            _tmpFatigueLevel = _cursor.getInt(_cursorIndexOfFatigueLevel);
            final int _tmpPathClarity;
            _tmpPathClarity = _cursor.getInt(_cursorIndexOfPathClarity);
            final int _tmpDifficultyLevel;
            _tmpDifficultyLevel = _cursor.getInt(_cursorIndexOfDifficultyLevel);
            final int _tmpMudRisk;
            _tmpMudRisk = _cursor.getInt(_cursorIndexOfMudRisk);
            final int _tmpAnimalEncounterRisk;
            _tmpAnimalEncounterRisk = _cursor.getInt(_cursorIndexOfAnimalEncounterRisk);
            final boolean _tmpWaterAvailability;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfWaterAvailability);
            _tmpWaterAvailability = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new TrailReviewEntity(_tmpReviewId,_tmpReviewerUid,_tmpHikeId,_tmpOverallRating,_tmpFatigueLevel,_tmpPathClarity,_tmpDifficultyLevel,_tmpMudRisk,_tmpAnimalEncounterRisk,_tmpWaterAvailability,_tmpCreatedAt);
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
  public Flow<Double> observeAvgDifficulty(final String hikeId) {
    final String _sql = "SELECT AVG(difficultyLevel) FROM trail_reviews WHERE hikeId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, hikeId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trail_reviews"}, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
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
  public Object getAvgOverallRating(final String hikeId,
      final Continuation<? super Double> $completion) {
    final String _sql = "SELECT AVG(overallRating) FROM trail_reviews WHERE hikeId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, hikeId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
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
  public Object getCount(final String hikeId, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM trail_reviews WHERE hikeId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, hikeId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
