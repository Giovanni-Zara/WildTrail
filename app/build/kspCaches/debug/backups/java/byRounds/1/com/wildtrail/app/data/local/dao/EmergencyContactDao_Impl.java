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
import com.wildtrail.app.data.local.entity.EmergencyContactEntity;
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
public final class EmergencyContactDao_Impl implements EmergencyContactDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EmergencyContactEntity> __insertionAdapterOfEmergencyContactEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public EmergencyContactDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEmergencyContactEntity = new EntityInsertionAdapter<EmergencyContactEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `emergency_contacts` (`contactId`,`userUid`,`name`,`phoneNumber`,`relationship`,`isPrimary`,`notifyOnFall`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EmergencyContactEntity entity) {
        statement.bindString(1, entity.getContactId());
        statement.bindString(2, entity.getUserUid());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getPhoneNumber());
        if (entity.getRelationship() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRelationship());
        }
        final int _tmp = entity.isPrimary() ? 1 : 0;
        statement.bindLong(6, _tmp);
        final int _tmp_1 = entity.getNotifyOnFall() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM emergency_contacts WHERE contactId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final EmergencyContactEntity contact,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEmergencyContactEntity.insert(contact);
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
  public Flow<List<EmergencyContactEntity>> observeForUser(final String uid) {
    final String _sql = "SELECT * FROM emergency_contacts WHERE userUid = ? ORDER BY isPrimary DESC, name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"emergency_contacts"}, new Callable<List<EmergencyContactEntity>>() {
      @Override
      @NonNull
      public List<EmergencyContactEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfContactId = CursorUtil.getColumnIndexOrThrow(_cursor, "contactId");
          final int _cursorIndexOfUserUid = CursorUtil.getColumnIndexOrThrow(_cursor, "userUid");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "relationship");
          final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
          final int _cursorIndexOfNotifyOnFall = CursorUtil.getColumnIndexOrThrow(_cursor, "notifyOnFall");
          final List<EmergencyContactEntity> _result = new ArrayList<EmergencyContactEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EmergencyContactEntity _item;
            final String _tmpContactId;
            _tmpContactId = _cursor.getString(_cursorIndexOfContactId);
            final String _tmpUserUid;
            _tmpUserUid = _cursor.getString(_cursorIndexOfUserUid);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhoneNumber;
            _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            final String _tmpRelationship;
            if (_cursor.isNull(_cursorIndexOfRelationship)) {
              _tmpRelationship = null;
            } else {
              _tmpRelationship = _cursor.getString(_cursorIndexOfRelationship);
            }
            final boolean _tmpIsPrimary;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
            _tmpIsPrimary = _tmp != 0;
            final boolean _tmpNotifyOnFall;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfNotifyOnFall);
            _tmpNotifyOnFall = _tmp_1 != 0;
            _item = new EmergencyContactEntity(_tmpContactId,_tmpUserUid,_tmpName,_tmpPhoneNumber,_tmpRelationship,_tmpIsPrimary,_tmpNotifyOnFall);
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
  public Object getPrimaryFor(final String uid,
      final Continuation<? super EmergencyContactEntity> $completion) {
    final String _sql = "SELECT * FROM emergency_contacts WHERE userUid = ? AND isPrimary = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EmergencyContactEntity>() {
      @Override
      @Nullable
      public EmergencyContactEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfContactId = CursorUtil.getColumnIndexOrThrow(_cursor, "contactId");
          final int _cursorIndexOfUserUid = CursorUtil.getColumnIndexOrThrow(_cursor, "userUid");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "relationship");
          final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
          final int _cursorIndexOfNotifyOnFall = CursorUtil.getColumnIndexOrThrow(_cursor, "notifyOnFall");
          final EmergencyContactEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpContactId;
            _tmpContactId = _cursor.getString(_cursorIndexOfContactId);
            final String _tmpUserUid;
            _tmpUserUid = _cursor.getString(_cursorIndexOfUserUid);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhoneNumber;
            _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            final String _tmpRelationship;
            if (_cursor.isNull(_cursorIndexOfRelationship)) {
              _tmpRelationship = null;
            } else {
              _tmpRelationship = _cursor.getString(_cursorIndexOfRelationship);
            }
            final boolean _tmpIsPrimary;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
            _tmpIsPrimary = _tmp != 0;
            final boolean _tmpNotifyOnFall;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfNotifyOnFall);
            _tmpNotifyOnFall = _tmp_1 != 0;
            _result = new EmergencyContactEntity(_tmpContactId,_tmpUserUid,_tmpName,_tmpPhoneNumber,_tmpRelationship,_tmpIsPrimary,_tmpNotifyOnFall);
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
  public Object getFallNotifyList(final String uid,
      final Continuation<? super List<EmergencyContactEntity>> $completion) {
    final String _sql = "SELECT * FROM emergency_contacts WHERE userUid = ? AND notifyOnFall = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EmergencyContactEntity>>() {
      @Override
      @NonNull
      public List<EmergencyContactEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfContactId = CursorUtil.getColumnIndexOrThrow(_cursor, "contactId");
          final int _cursorIndexOfUserUid = CursorUtil.getColumnIndexOrThrow(_cursor, "userUid");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
          final int _cursorIndexOfRelationship = CursorUtil.getColumnIndexOrThrow(_cursor, "relationship");
          final int _cursorIndexOfIsPrimary = CursorUtil.getColumnIndexOrThrow(_cursor, "isPrimary");
          final int _cursorIndexOfNotifyOnFall = CursorUtil.getColumnIndexOrThrow(_cursor, "notifyOnFall");
          final List<EmergencyContactEntity> _result = new ArrayList<EmergencyContactEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EmergencyContactEntity _item;
            final String _tmpContactId;
            _tmpContactId = _cursor.getString(_cursorIndexOfContactId);
            final String _tmpUserUid;
            _tmpUserUid = _cursor.getString(_cursorIndexOfUserUid);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpPhoneNumber;
            _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
            final String _tmpRelationship;
            if (_cursor.isNull(_cursorIndexOfRelationship)) {
              _tmpRelationship = null;
            } else {
              _tmpRelationship = _cursor.getString(_cursorIndexOfRelationship);
            }
            final boolean _tmpIsPrimary;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPrimary);
            _tmpIsPrimary = _tmp != 0;
            final boolean _tmpNotifyOnFall;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfNotifyOnFall);
            _tmpNotifyOnFall = _tmp_1 != 0;
            _item = new EmergencyContactEntity(_tmpContactId,_tmpUserUid,_tmpName,_tmpPhoneNumber,_tmpRelationship,_tmpIsPrimary,_tmpNotifyOnFall);
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
