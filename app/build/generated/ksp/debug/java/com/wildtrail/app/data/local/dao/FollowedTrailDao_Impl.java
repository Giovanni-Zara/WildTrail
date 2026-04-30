package com.wildtrail.app.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.wildtrail.app.data.local.entity.FollowedTrailEntity;
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
public final class FollowedTrailDao_Impl implements FollowedTrailDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FollowedTrailEntity> __insertionAdapterOfFollowedTrailEntity;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  public FollowedTrailDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFollowedTrailEntity = new EntityInsertionAdapter<FollowedTrailEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `followed_trails` (`userUid`,`hikeId`,`notifyOnNewReview`,`createdAt`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FollowedTrailEntity entity) {
        statement.bindString(1, entity.getUserUid());
        statement.bindString(2, entity.getHikeId());
        final int _tmp = entity.getNotifyOnNewReview() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindLong(4, entity.getCreatedAt());
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM followed_trails WHERE userUid = ? AND hikeId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final FollowedTrailEntity trail,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFollowedTrailEntity.insert(trail);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final String uid, final String hikeId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, uid);
        _argIndex = 2;
        _stmt.bindString(_argIndex, hikeId);
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
          __preparedStmtOfDelete.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<FollowedTrailEntity>> observeForUser(final String uid) {
    final String _sql = "SELECT * FROM followed_trails WHERE userUid = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"followed_trails"}, new Callable<List<FollowedTrailEntity>>() {
      @Override
      @NonNull
      public List<FollowedTrailEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserUid = CursorUtil.getColumnIndexOrThrow(_cursor, "userUid");
          final int _cursorIndexOfHikeId = CursorUtil.getColumnIndexOrThrow(_cursor, "hikeId");
          final int _cursorIndexOfNotifyOnNewReview = CursorUtil.getColumnIndexOrThrow(_cursor, "notifyOnNewReview");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<FollowedTrailEntity> _result = new ArrayList<FollowedTrailEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FollowedTrailEntity _item;
            final String _tmpUserUid;
            _tmpUserUid = _cursor.getString(_cursorIndexOfUserUid);
            final String _tmpHikeId;
            _tmpHikeId = _cursor.getString(_cursorIndexOfHikeId);
            final boolean _tmpNotifyOnNewReview;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfNotifyOnNewReview);
            _tmpNotifyOnNewReview = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new FollowedTrailEntity(_tmpUserUid,_tmpHikeId,_tmpNotifyOnNewReview,_tmpCreatedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
