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
import com.wildtrail.app.data.local.converter.Converters;
import com.wildtrail.app.data.local.entity.HikeCommentEntity;
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
public final class HikeCommentDao_Impl implements HikeCommentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HikeCommentEntity> __insertionAdapterOfHikeCommentEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public HikeCommentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHikeCommentEntity = new EntityInsertionAdapter<HikeCommentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `hike_comments` (`commentId`,`authorUid`,`hikeId`,`text`,`photoUrls`,`createdAt`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HikeCommentEntity entity) {
        statement.bindString(1, entity.getCommentId());
        statement.bindString(2, entity.getAuthorUid());
        statement.bindString(3, entity.getHikeId());
        statement.bindString(4, entity.getText());
        final String _tmp = Converters.INSTANCE.stringListToJson(entity.getPhotoUrls());
        statement.bindString(5, _tmp);
        statement.bindLong(6, entity.getCreatedAt());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM hike_comments WHERE commentId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final HikeCommentEntity comment,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfHikeCommentEntity.insert(comment);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertAll(final List<HikeCommentEntity> comments,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfHikeCommentEntity.insert(comments);
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
  public Flow<List<HikeCommentEntity>> observeForHike(final String hikeId) {
    final String _sql = "SELECT * FROM hike_comments WHERE hikeId = ? ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, hikeId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"hike_comments"}, new Callable<List<HikeCommentEntity>>() {
      @Override
      @NonNull
      public List<HikeCommentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCommentId = CursorUtil.getColumnIndexOrThrow(_cursor, "commentId");
          final int _cursorIndexOfAuthorUid = CursorUtil.getColumnIndexOrThrow(_cursor, "authorUid");
          final int _cursorIndexOfHikeId = CursorUtil.getColumnIndexOrThrow(_cursor, "hikeId");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfPhotoUrls = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUrls");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<HikeCommentEntity> _result = new ArrayList<HikeCommentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HikeCommentEntity _item;
            final String _tmpCommentId;
            _tmpCommentId = _cursor.getString(_cursorIndexOfCommentId);
            final String _tmpAuthorUid;
            _tmpAuthorUid = _cursor.getString(_cursorIndexOfAuthorUid);
            final String _tmpHikeId;
            _tmpHikeId = _cursor.getString(_cursorIndexOfHikeId);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final List<String> _tmpPhotoUrls;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfPhotoUrls);
            _tmpPhotoUrls = Converters.INSTANCE.jsonToStringList(_tmp);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new HikeCommentEntity(_tmpCommentId,_tmpAuthorUid,_tmpHikeId,_tmpText,_tmpPhotoUrls,_tmpCreatedAt);
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
