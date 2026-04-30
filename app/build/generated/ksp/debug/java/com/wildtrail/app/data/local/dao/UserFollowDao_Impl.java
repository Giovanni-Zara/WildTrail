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
import com.wildtrail.app.data.local.entity.UserFollowEntity;
import java.lang.Boolean;
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
public final class UserFollowDao_Impl implements UserFollowDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UserFollowEntity> __insertionAdapterOfUserFollowEntity;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  public UserFollowDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUserFollowEntity = new EntityInsertionAdapter<UserFollowEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `user_follows` (`followerUid`,`followeeUid`,`notifyOnNewHike`,`createdAt`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserFollowEntity entity) {
        statement.bindString(1, entity.getFollowerUid());
        statement.bindString(2, entity.getFolloweeUid());
        final int _tmp = entity.getNotifyOnNewHike() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindLong(4, entity.getCreatedAt());
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM user_follows WHERE followerUid = ? AND followeeUid = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final UserFollowEntity follow,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserFollowEntity.insert(follow);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final String follower, final String followee,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, follower);
        _argIndex = 2;
        _stmt.bindString(_argIndex, followee);
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
  public Flow<List<UserFollowEntity>> observeFollowing(final String uid) {
    final String _sql = "SELECT * FROM user_follows WHERE followerUid = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"user_follows"}, new Callable<List<UserFollowEntity>>() {
      @Override
      @NonNull
      public List<UserFollowEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfFollowerUid = CursorUtil.getColumnIndexOrThrow(_cursor, "followerUid");
          final int _cursorIndexOfFolloweeUid = CursorUtil.getColumnIndexOrThrow(_cursor, "followeeUid");
          final int _cursorIndexOfNotifyOnNewHike = CursorUtil.getColumnIndexOrThrow(_cursor, "notifyOnNewHike");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<UserFollowEntity> _result = new ArrayList<UserFollowEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserFollowEntity _item;
            final String _tmpFollowerUid;
            _tmpFollowerUid = _cursor.getString(_cursorIndexOfFollowerUid);
            final String _tmpFolloweeUid;
            _tmpFolloweeUid = _cursor.getString(_cursorIndexOfFolloweeUid);
            final boolean _tmpNotifyOnNewHike;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfNotifyOnNewHike);
            _tmpNotifyOnNewHike = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new UserFollowEntity(_tmpFollowerUid,_tmpFolloweeUid,_tmpNotifyOnNewHike,_tmpCreatedAt);
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
  public Flow<List<UserFollowEntity>> observeFollowers(final String uid) {
    final String _sql = "SELECT * FROM user_follows WHERE followeeUid = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"user_follows"}, new Callable<List<UserFollowEntity>>() {
      @Override
      @NonNull
      public List<UserFollowEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfFollowerUid = CursorUtil.getColumnIndexOrThrow(_cursor, "followerUid");
          final int _cursorIndexOfFolloweeUid = CursorUtil.getColumnIndexOrThrow(_cursor, "followeeUid");
          final int _cursorIndexOfNotifyOnNewHike = CursorUtil.getColumnIndexOrThrow(_cursor, "notifyOnNewHike");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<UserFollowEntity> _result = new ArrayList<UserFollowEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserFollowEntity _item;
            final String _tmpFollowerUid;
            _tmpFollowerUid = _cursor.getString(_cursorIndexOfFollowerUid);
            final String _tmpFolloweeUid;
            _tmpFolloweeUid = _cursor.getString(_cursorIndexOfFolloweeUid);
            final boolean _tmpNotifyOnNewHike;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfNotifyOnNewHike);
            _tmpNotifyOnNewHike = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new UserFollowEntity(_tmpFollowerUid,_tmpFolloweeUid,_tmpNotifyOnNewHike,_tmpCreatedAt);
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
  public Flow<Boolean> observeIsFollowing(final String follower, final String followee) {
    final String _sql = "\n"
            + "        SELECT EXISTS(\n"
            + "            SELECT 1 FROM user_follows\n"
            + "             WHERE followerUid = ? AND followeeUid = ?\n"
            + "        )\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, follower);
    _argIndex = 2;
    _statement.bindString(_argIndex, followee);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"user_follows"}, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
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
