package com.wildtrail.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.wildtrail.app.data.local.entity.UserEntity;
import java.lang.Class;
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
public final class UserDao_Impl implements UserDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UserEntity> __insertionAdapterOfUserEntity;

  private final EntityDeletionOrUpdateAdapter<UserEntity> __updateAdapterOfUserEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfClear;

  public UserDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUserEntity = new EntityInsertionAdapter<UserEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `users` (`firebaseUid`,`username`,`age`,`country`,`level`,`xpPoints`,`totalDistanceKm`,`totalHikesCount`,`profilePictureUrl`,`bio`,`createdAt`,`lastActive`,`isPublic`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserEntity entity) {
        statement.bindString(1, entity.getFirebaseUid());
        statement.bindString(2, entity.getUsername());
        if (entity.getAge() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getAge());
        }
        if (entity.getCountry() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCountry());
        }
        statement.bindLong(5, entity.getLevel());
        statement.bindLong(6, entity.getXpPoints());
        statement.bindDouble(7, entity.getTotalDistanceKm());
        statement.bindLong(8, entity.getTotalHikesCount());
        if (entity.getProfilePictureUrl() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getProfilePictureUrl());
        }
        if (entity.getBio() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getBio());
        }
        statement.bindLong(11, entity.getCreatedAt());
        statement.bindLong(12, entity.getLastActive());
        final int _tmp = entity.isPublic() ? 1 : 0;
        statement.bindLong(13, _tmp);
      }
    };
    this.__updateAdapterOfUserEntity = new EntityDeletionOrUpdateAdapter<UserEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `users` SET `firebaseUid` = ?,`username` = ?,`age` = ?,`country` = ?,`level` = ?,`xpPoints` = ?,`totalDistanceKm` = ?,`totalHikesCount` = ?,`profilePictureUrl` = ?,`bio` = ?,`createdAt` = ?,`lastActive` = ?,`isPublic` = ? WHERE `firebaseUid` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserEntity entity) {
        statement.bindString(1, entity.getFirebaseUid());
        statement.bindString(2, entity.getUsername());
        if (entity.getAge() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getAge());
        }
        if (entity.getCountry() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCountry());
        }
        statement.bindLong(5, entity.getLevel());
        statement.bindLong(6, entity.getXpPoints());
        statement.bindDouble(7, entity.getTotalDistanceKm());
        statement.bindLong(8, entity.getTotalHikesCount());
        if (entity.getProfilePictureUrl() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getProfilePictureUrl());
        }
        if (entity.getBio() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getBio());
        }
        statement.bindLong(11, entity.getCreatedAt());
        statement.bindLong(12, entity.getLastActive());
        final int _tmp = entity.isPublic() ? 1 : 0;
        statement.bindLong(13, _tmp);
        statement.bindString(14, entity.getFirebaseUid());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM users WHERE firebaseUid = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClear = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM users";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final UserEntity user, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserEntity.insert(user);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertAll(final List<UserEntity> users,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserEntity.insert(users);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final UserEntity user, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfUserEntity.handle(user);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final String uid, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, uid);
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
  public Object getById(final String uid, final Continuation<? super UserEntity> $completion) {
    final String _sql = "SELECT * FROM users WHERE firebaseUid = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UserEntity>() {
      @Override
      @Nullable
      public UserEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfFirebaseUid = CursorUtil.getColumnIndexOrThrow(_cursor, "firebaseUid");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfAge = CursorUtil.getColumnIndexOrThrow(_cursor, "age");
          final int _cursorIndexOfCountry = CursorUtil.getColumnIndexOrThrow(_cursor, "country");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfXpPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "xpPoints");
          final int _cursorIndexOfTotalDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDistanceKm");
          final int _cursorIndexOfTotalHikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalHikesCount");
          final int _cursorIndexOfProfilePictureUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "profilePictureUrl");
          final int _cursorIndexOfBio = CursorUtil.getColumnIndexOrThrow(_cursor, "bio");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastActive = CursorUtil.getColumnIndexOrThrow(_cursor, "lastActive");
          final int _cursorIndexOfIsPublic = CursorUtil.getColumnIndexOrThrow(_cursor, "isPublic");
          final UserEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpFirebaseUid;
            _tmpFirebaseUid = _cursor.getString(_cursorIndexOfFirebaseUid);
            final String _tmpUsername;
            _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            final Integer _tmpAge;
            if (_cursor.isNull(_cursorIndexOfAge)) {
              _tmpAge = null;
            } else {
              _tmpAge = _cursor.getInt(_cursorIndexOfAge);
            }
            final String _tmpCountry;
            if (_cursor.isNull(_cursorIndexOfCountry)) {
              _tmpCountry = null;
            } else {
              _tmpCountry = _cursor.getString(_cursorIndexOfCountry);
            }
            final int _tmpLevel;
            _tmpLevel = _cursor.getInt(_cursorIndexOfLevel);
            final int _tmpXpPoints;
            _tmpXpPoints = _cursor.getInt(_cursorIndexOfXpPoints);
            final float _tmpTotalDistanceKm;
            _tmpTotalDistanceKm = _cursor.getFloat(_cursorIndexOfTotalDistanceKm);
            final int _tmpTotalHikesCount;
            _tmpTotalHikesCount = _cursor.getInt(_cursorIndexOfTotalHikesCount);
            final String _tmpProfilePictureUrl;
            if (_cursor.isNull(_cursorIndexOfProfilePictureUrl)) {
              _tmpProfilePictureUrl = null;
            } else {
              _tmpProfilePictureUrl = _cursor.getString(_cursorIndexOfProfilePictureUrl);
            }
            final String _tmpBio;
            if (_cursor.isNull(_cursorIndexOfBio)) {
              _tmpBio = null;
            } else {
              _tmpBio = _cursor.getString(_cursorIndexOfBio);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpLastActive;
            _tmpLastActive = _cursor.getLong(_cursorIndexOfLastActive);
            final boolean _tmpIsPublic;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPublic);
            _tmpIsPublic = _tmp != 0;
            _result = new UserEntity(_tmpFirebaseUid,_tmpUsername,_tmpAge,_tmpCountry,_tmpLevel,_tmpXpPoints,_tmpTotalDistanceKm,_tmpTotalHikesCount,_tmpProfilePictureUrl,_tmpBio,_tmpCreatedAt,_tmpLastActive,_tmpIsPublic);
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
  public Flow<UserEntity> observeById(final String uid) {
    final String _sql = "SELECT * FROM users WHERE firebaseUid = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"users"}, new Callable<UserEntity>() {
      @Override
      @Nullable
      public UserEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfFirebaseUid = CursorUtil.getColumnIndexOrThrow(_cursor, "firebaseUid");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfAge = CursorUtil.getColumnIndexOrThrow(_cursor, "age");
          final int _cursorIndexOfCountry = CursorUtil.getColumnIndexOrThrow(_cursor, "country");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfXpPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "xpPoints");
          final int _cursorIndexOfTotalDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDistanceKm");
          final int _cursorIndexOfTotalHikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalHikesCount");
          final int _cursorIndexOfProfilePictureUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "profilePictureUrl");
          final int _cursorIndexOfBio = CursorUtil.getColumnIndexOrThrow(_cursor, "bio");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastActive = CursorUtil.getColumnIndexOrThrow(_cursor, "lastActive");
          final int _cursorIndexOfIsPublic = CursorUtil.getColumnIndexOrThrow(_cursor, "isPublic");
          final UserEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpFirebaseUid;
            _tmpFirebaseUid = _cursor.getString(_cursorIndexOfFirebaseUid);
            final String _tmpUsername;
            _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            final Integer _tmpAge;
            if (_cursor.isNull(_cursorIndexOfAge)) {
              _tmpAge = null;
            } else {
              _tmpAge = _cursor.getInt(_cursorIndexOfAge);
            }
            final String _tmpCountry;
            if (_cursor.isNull(_cursorIndexOfCountry)) {
              _tmpCountry = null;
            } else {
              _tmpCountry = _cursor.getString(_cursorIndexOfCountry);
            }
            final int _tmpLevel;
            _tmpLevel = _cursor.getInt(_cursorIndexOfLevel);
            final int _tmpXpPoints;
            _tmpXpPoints = _cursor.getInt(_cursorIndexOfXpPoints);
            final float _tmpTotalDistanceKm;
            _tmpTotalDistanceKm = _cursor.getFloat(_cursorIndexOfTotalDistanceKm);
            final int _tmpTotalHikesCount;
            _tmpTotalHikesCount = _cursor.getInt(_cursorIndexOfTotalHikesCount);
            final String _tmpProfilePictureUrl;
            if (_cursor.isNull(_cursorIndexOfProfilePictureUrl)) {
              _tmpProfilePictureUrl = null;
            } else {
              _tmpProfilePictureUrl = _cursor.getString(_cursorIndexOfProfilePictureUrl);
            }
            final String _tmpBio;
            if (_cursor.isNull(_cursorIndexOfBio)) {
              _tmpBio = null;
            } else {
              _tmpBio = _cursor.getString(_cursorIndexOfBio);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpLastActive;
            _tmpLastActive = _cursor.getLong(_cursorIndexOfLastActive);
            final boolean _tmpIsPublic;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPublic);
            _tmpIsPublic = _tmp != 0;
            _result = new UserEntity(_tmpFirebaseUid,_tmpUsername,_tmpAge,_tmpCountry,_tmpLevel,_tmpXpPoints,_tmpTotalDistanceKm,_tmpTotalHikesCount,_tmpProfilePictureUrl,_tmpBio,_tmpCreatedAt,_tmpLastActive,_tmpIsPublic);
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
  public Flow<List<UserEntity>> observeLeaderboard(final int limit) {
    final String _sql = "SELECT * FROM users WHERE isPublic = 1 ORDER BY xpPoints DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"users"}, new Callable<List<UserEntity>>() {
      @Override
      @NonNull
      public List<UserEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfFirebaseUid = CursorUtil.getColumnIndexOrThrow(_cursor, "firebaseUid");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfAge = CursorUtil.getColumnIndexOrThrow(_cursor, "age");
          final int _cursorIndexOfCountry = CursorUtil.getColumnIndexOrThrow(_cursor, "country");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfXpPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "xpPoints");
          final int _cursorIndexOfTotalDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDistanceKm");
          final int _cursorIndexOfTotalHikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalHikesCount");
          final int _cursorIndexOfProfilePictureUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "profilePictureUrl");
          final int _cursorIndexOfBio = CursorUtil.getColumnIndexOrThrow(_cursor, "bio");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastActive = CursorUtil.getColumnIndexOrThrow(_cursor, "lastActive");
          final int _cursorIndexOfIsPublic = CursorUtil.getColumnIndexOrThrow(_cursor, "isPublic");
          final List<UserEntity> _result = new ArrayList<UserEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserEntity _item;
            final String _tmpFirebaseUid;
            _tmpFirebaseUid = _cursor.getString(_cursorIndexOfFirebaseUid);
            final String _tmpUsername;
            _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            final Integer _tmpAge;
            if (_cursor.isNull(_cursorIndexOfAge)) {
              _tmpAge = null;
            } else {
              _tmpAge = _cursor.getInt(_cursorIndexOfAge);
            }
            final String _tmpCountry;
            if (_cursor.isNull(_cursorIndexOfCountry)) {
              _tmpCountry = null;
            } else {
              _tmpCountry = _cursor.getString(_cursorIndexOfCountry);
            }
            final int _tmpLevel;
            _tmpLevel = _cursor.getInt(_cursorIndexOfLevel);
            final int _tmpXpPoints;
            _tmpXpPoints = _cursor.getInt(_cursorIndexOfXpPoints);
            final float _tmpTotalDistanceKm;
            _tmpTotalDistanceKm = _cursor.getFloat(_cursorIndexOfTotalDistanceKm);
            final int _tmpTotalHikesCount;
            _tmpTotalHikesCount = _cursor.getInt(_cursorIndexOfTotalHikesCount);
            final String _tmpProfilePictureUrl;
            if (_cursor.isNull(_cursorIndexOfProfilePictureUrl)) {
              _tmpProfilePictureUrl = null;
            } else {
              _tmpProfilePictureUrl = _cursor.getString(_cursorIndexOfProfilePictureUrl);
            }
            final String _tmpBio;
            if (_cursor.isNull(_cursorIndexOfBio)) {
              _tmpBio = null;
            } else {
              _tmpBio = _cursor.getString(_cursorIndexOfBio);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpLastActive;
            _tmpLastActive = _cursor.getLong(_cursorIndexOfLastActive);
            final boolean _tmpIsPublic;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPublic);
            _tmpIsPublic = _tmp != 0;
            _item = new UserEntity(_tmpFirebaseUid,_tmpUsername,_tmpAge,_tmpCountry,_tmpLevel,_tmpXpPoints,_tmpTotalDistanceKm,_tmpTotalHikesCount,_tmpProfilePictureUrl,_tmpBio,_tmpCreatedAt,_tmpLastActive,_tmpIsPublic);
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
  public Object searchByUsername(final String q,
      final Continuation<? super List<UserEntity>> $completion) {
    final String _sql = "SELECT * FROM users WHERE username LIKE '%' || ? || '%' AND isPublic = 1 LIMIT 50";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, q);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<UserEntity>>() {
      @Override
      @NonNull
      public List<UserEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfFirebaseUid = CursorUtil.getColumnIndexOrThrow(_cursor, "firebaseUid");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfAge = CursorUtil.getColumnIndexOrThrow(_cursor, "age");
          final int _cursorIndexOfCountry = CursorUtil.getColumnIndexOrThrow(_cursor, "country");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfXpPoints = CursorUtil.getColumnIndexOrThrow(_cursor, "xpPoints");
          final int _cursorIndexOfTotalDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDistanceKm");
          final int _cursorIndexOfTotalHikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalHikesCount");
          final int _cursorIndexOfProfilePictureUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "profilePictureUrl");
          final int _cursorIndexOfBio = CursorUtil.getColumnIndexOrThrow(_cursor, "bio");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastActive = CursorUtil.getColumnIndexOrThrow(_cursor, "lastActive");
          final int _cursorIndexOfIsPublic = CursorUtil.getColumnIndexOrThrow(_cursor, "isPublic");
          final List<UserEntity> _result = new ArrayList<UserEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserEntity _item;
            final String _tmpFirebaseUid;
            _tmpFirebaseUid = _cursor.getString(_cursorIndexOfFirebaseUid);
            final String _tmpUsername;
            _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            final Integer _tmpAge;
            if (_cursor.isNull(_cursorIndexOfAge)) {
              _tmpAge = null;
            } else {
              _tmpAge = _cursor.getInt(_cursorIndexOfAge);
            }
            final String _tmpCountry;
            if (_cursor.isNull(_cursorIndexOfCountry)) {
              _tmpCountry = null;
            } else {
              _tmpCountry = _cursor.getString(_cursorIndexOfCountry);
            }
            final int _tmpLevel;
            _tmpLevel = _cursor.getInt(_cursorIndexOfLevel);
            final int _tmpXpPoints;
            _tmpXpPoints = _cursor.getInt(_cursorIndexOfXpPoints);
            final float _tmpTotalDistanceKm;
            _tmpTotalDistanceKm = _cursor.getFloat(_cursorIndexOfTotalDistanceKm);
            final int _tmpTotalHikesCount;
            _tmpTotalHikesCount = _cursor.getInt(_cursorIndexOfTotalHikesCount);
            final String _tmpProfilePictureUrl;
            if (_cursor.isNull(_cursorIndexOfProfilePictureUrl)) {
              _tmpProfilePictureUrl = null;
            } else {
              _tmpProfilePictureUrl = _cursor.getString(_cursorIndexOfProfilePictureUrl);
            }
            final String _tmpBio;
            if (_cursor.isNull(_cursorIndexOfBio)) {
              _tmpBio = null;
            } else {
              _tmpBio = _cursor.getString(_cursorIndexOfBio);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpLastActive;
            _tmpLastActive = _cursor.getLong(_cursorIndexOfLastActive);
            final boolean _tmpIsPublic;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPublic);
            _tmpIsPublic = _tmp != 0;
            _item = new UserEntity(_tmpFirebaseUid,_tmpUsername,_tmpAge,_tmpCountry,_tmpLevel,_tmpXpPoints,_tmpTotalDistanceKm,_tmpTotalHikesCount,_tmpProfilePictureUrl,_tmpBio,_tmpCreatedAt,_tmpLastActive,_tmpIsPublic);
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
