package com.wildtrail.app.data.repository

import android.util.Log
import com.wildtrail.app.data.local.dao.AchievementDao
import com.wildtrail.app.data.local.entity.toDomain
import com.wildtrail.app.data.local.entity.toEntity
import com.wildtrail.app.data.remote.FirestoreService
import com.wildtrail.app.data.remote.dto.toDomain
import com.wildtrail.app.domain.model.AchievementDefinition
import com.wildtrail.app.domain.model.UserAchievement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AchievementRepository(
    private val achievementDao: AchievementDao,
    private val firestore: FirestoreService,
) {

    /** Pull the static catalogue once at app start (call from a coroutine).
     *  Wrapped: Firestore being unreachable / locked down must not crash. */
    suspend fun syncDefinitions() {
        runCatching {
            val defs = firestore.fetchAchievementDefinitions().map { it.toDomain().toEntity() }
            if (defs.isNotEmpty()) achievementDao.upsertDefinitions(defs)
        }.onFailure { Log.w(TAG, "Achievement catalogue sync skipped", it) }
    }

    fun observeAll(): Flow<List<AchievementDefinition>> =
        achievementDao.observeAllDefinitions().map { list -> list.map { it.toDomain() } }

    fun observeEarned(userUid: String): Flow<List<AchievementDefinition>> =
        achievementDao.observeEarnedFor(userUid).map { list -> list.map { it.toDomain() } }

    suspend fun award(award: UserAchievement) {
        achievementDao.upsertEarned(award.toEntity())
        runCatching {
            firestore.upsertEarnedAchievement(
                com.wildtrail.app.data.remote.dto.UserAchievementDto(
                    userUid = award.userUid,
                    achievementId = award.achievementId,
                    earnedAt = award.earnedAt,
                ),
            )
        }.onFailure { Log.w(TAG, "Achievement award sync skipped", it) }
    }

    private companion object { const val TAG = "AchievementRepository" }
}
