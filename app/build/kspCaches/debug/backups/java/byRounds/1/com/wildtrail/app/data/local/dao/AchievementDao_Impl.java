package com.wildtrail.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.wildtrail.app.data.local.converter.Converters;
import com.wildtrail.app.data.local.entity.AchievementDefinitionEntity;
import com.wildtrail.app.data.local.entity.UserAchievementEntity;
import com.wildtrail.app.domain.model.AchievementCategory;
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
public final class AchievementDao_Impl implements AchievementDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AchievementDefinitionEntity> __insertionAdapterOfAchievementDefinitionEntity;

  private final EntityInsertionAdapter<UserAchievementEntity> __insertionAdapterOfUserAchievementEntity;

  public AchievementDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAchievementDefinitionEntity = new EntityInsertionAdapter<AchievementDefinitionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `achievement_definitions` (`achievementId`,`name`,`description`,`iconUrl`,`xpReward`,`category`,`thresholdValue`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AchievementDefinitionEntity entity) {
        statement.bindString(1, entity.getAchievementId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDescription());
        if (entity.getIconUrl() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getIconUrl());
        }
        statement.bindLong(5, entity.getXpReward());
        final String _tmp = Converters.INSTANCE.achievementCategoryToString(entity.getCategory());
        statement.bindString(6, _tmp);
        statement.bindDouble(7, entity.getThresholdValue());
      }
    };
    this.__insertionAdapterOfUserAchievementEntity = new EntityInsertionAdapter<UserAchievementEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `user_achievements` (`userUid`,`achievementId`,`earnedAt`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserAchievementEntity entity) {
        statement.bindString(1, entity.getUserUid());
        statement.bindString(2, entity.getAchievementId());
        statement.bindLong(3, entity.getEarnedAt());
      }
    };
  }

  @Override
  public Object upsertDefinitions(final List<AchievementDefinitionEntity> defs,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAchievementDefinitionEntity.insert(defs);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertEarned(final UserAchievementEntity earned,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserAchievementEntity.insert(earned);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AchievementDefinitionEntity>> observeAllDefinitions() {
    final String _sql = "SELECT * FROM achievement_definitions ORDER BY xpReward ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"achievement_definitions"}, new Callable<List<AchievementDefinitionEntity>>() {
      @Override
      @NonNull
      public List<AchievementDefinitionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAchievementId = CursorUtil.getColumnIndexOrThrow(_cursor, "achievementId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIconUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "iconUrl");
          final int _cursorIndexOfXpReward = CursorUtil.getColumnIndexOrThrow(_cursor, "xpReward");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfThresholdValue = CursorUtil.getColumnIndexOrThrow(_cursor, "thresholdValue");
          final List<AchievementDefinitionEntity> _result = new ArrayList<AchievementDefinitionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AchievementDefinitionEntity _item;
            final String _tmpAchievementId;
            _tmpAchievementId = _cursor.getString(_cursorIndexOfAchievementId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpIconUrl;
            if (_cursor.isNull(_cursorIndexOfIconUrl)) {
              _tmpIconUrl = null;
            } else {
              _tmpIconUrl = _cursor.getString(_cursorIndexOfIconUrl);
            }
            final int _tmpXpReward;
            _tmpXpReward = _cursor.getInt(_cursorIndexOfXpReward);
            final AchievementCategory _tmpCategory;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfCategory);
            _tmpCategory = Converters.INSTANCE.stringToAchievementCategory(_tmp);
            final float _tmpThresholdValue;
            _tmpThresholdValue = _cursor.getFloat(_cursorIndexOfThresholdValue);
            _item = new AchievementDefinitionEntity(_tmpAchievementId,_tmpName,_tmpDescription,_tmpIconUrl,_tmpXpReward,_tmpCategory,_tmpThresholdValue);
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
  public Flow<List<AchievementDefinitionEntity>> observeEarnedFor(final String uid) {
    final String _sql = "\n"
            + "        SELECT d.* FROM achievement_definitions d\n"
            + "         INNER JOIN user_achievements ua ON ua.achievementId = d.achievementId\n"
            + "         WHERE ua.userUid = ?\n"
            + "         ORDER BY ua.earnedAt DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"achievement_definitions",
        "user_achievements"}, new Callable<List<AchievementDefinitionEntity>>() {
      @Override
      @NonNull
      public List<AchievementDefinitionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAchievementId = CursorUtil.getColumnIndexOrThrow(_cursor, "achievementId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIconUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "iconUrl");
          final int _cursorIndexOfXpReward = CursorUtil.getColumnIndexOrThrow(_cursor, "xpReward");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfThresholdValue = CursorUtil.getColumnIndexOrThrow(_cursor, "thresholdValue");
          final List<AchievementDefinitionEntity> _result = new ArrayList<AchievementDefinitionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AchievementDefinitionEntity _item;
            final String _tmpAchievementId;
            _tmpAchievementId = _cursor.getString(_cursorIndexOfAchievementId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpIconUrl;
            if (_cursor.isNull(_cursorIndexOfIconUrl)) {
              _tmpIconUrl = null;
            } else {
              _tmpIconUrl = _cursor.getString(_cursorIndexOfIconUrl);
            }
            final int _tmpXpReward;
            _tmpXpReward = _cursor.getInt(_cursorIndexOfXpReward);
            final AchievementCategory _tmpCategory;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfCategory);
            _tmpCategory = Converters.INSTANCE.stringToAchievementCategory(_tmp);
            final float _tmpThresholdValue;
            _tmpThresholdValue = _cursor.getFloat(_cursorIndexOfThresholdValue);
            _item = new AchievementDefinitionEntity(_tmpAchievementId,_tmpName,_tmpDescription,_tmpIconUrl,_tmpXpReward,_tmpCategory,_tmpThresholdValue);
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
  public Object hasEarned(final String uid, final String achievementId,
      final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM user_achievements WHERE userUid = ? AND achievementId = ?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, uid);
    _argIndex = 2;
    _statement.bindString(_argIndex, achievementId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
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
