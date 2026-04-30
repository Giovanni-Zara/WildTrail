package com.wildtrail.app.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wildtrail.app.data.local.WildTrailDatabase
import com.wildtrail.app.data.local.entity.UserEntity
import com.wildtrail.app.data.remote.FirestoreService
import com.wildtrail.app.data.remote.dto.HikeLogDto
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.SurfaceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for [HikeLogRepository] using a real in-memory Room DB and a
 * hand-rolled fake [FirestoreService] that backs its observations with a
 * MutableStateFlow we can write to from the test.
 *
 * EXPECTED for each test: the repository emits the expected domain models.
 *
 * No Mockito needed: the [FirestoreService] base class accepts a nullable
 * [com.google.firebase.firestore.FirebaseFirestore], so the fake can pass
 * `null` and override every method it actually needs.
 */
@RunWith(AndroidJUnit4::class)
class HikeLogRepositoryTest {

    private lateinit var db: WildTrailDatabase
    private lateinit var fakeFirestore: FakeFirestoreService
    private lateinit var repo: HikeLogRepository
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Before
    fun setUp() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, WildTrailDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        // FK constraint: every hike needs an existing user row.
        db.userDao().upsert(
            UserEntity(
                firebaseUid = "uid-1", username = "g", age = null, country = null,
                level = 1, xpPoints = 0, totalDistanceKm = 0f, totalHikesCount = 0,
                profilePictureUrl = null, bio = null, createdAt = 0L, lastActive = 0L,
                isPublic = true,
            ),
        )
        fakeFirestore = FakeFirestoreService()
        repo = HikeLogRepository(
            hikeLogDao = db.hikeLogDao(),
            firestore = fakeFirestore,
            externalScope = scope,
        )
    }

    @After
    fun tearDown() = db.close()

    /**
     * EXPECTED: after saving a hike via the repository, observeMyHikes
     *           emits a list containing that hike.
     */
    @Test
    fun saveHike_appearsInLocalFeed() = runBlocking {
        val hike = sampleHike("h1")
        repo.saveHike(hike)
        val feed = repo.observeMyHikes("uid-1").first()
        assertEquals(1, feed.size)
        assertEquals("h1", feed.first().hikeId)
    }

    /**
     * EXPECTED: saveHike forwards the hike to Firestore (verified by checking
     *           the fake's recorded calls).
     */
    @Test
    fun saveHike_pushesToFirestore() = runBlocking {
        val hike = sampleHike("h1")
        repo.saveHike(hike)
        assertEquals(1, fakeFirestore.upsertHikeCalls.size)
        assertEquals("h1", fakeFirestore.upsertHikeCalls.first().hikeId)
    }

    private fun sampleHike(id: String) = HikeLog(
        hikeId = id,
        creatorFirebaseUid = "uid-1",
        workoutId = null,
        title = "Sample",
        description = null,
        avgSpeedKmh = 4f,
        stepCount = 0,
        caloriesBurned = 100,
        coverPhotoUrl = null,
        xpEarned = 10,
        likesCount = 0,
        surfaceType = SurfaceType.MOUNTAIN,
        lengthKm = 1f,
        durationSeconds = 600L,
        startedAt = 0L,
        endedAt = 0L,
        elevationGainMeters = 50,
        routeCoordinates = emptyList(),
        isPrivate = false,
    )
}

/** Minimal fake [FirestoreService]. Pass `null` to the parent so we never
 *  hit Firebase initialisation; override only the methods this test exercises. */
class FakeFirestoreService : FirestoreService(db = null) {
    val upsertHikeCalls = mutableListOf<HikeLogDto>()
    private val publicHikes = MutableStateFlow<List<HikeLogDto>>(emptyList())

    override suspend fun upsertHike(dto: HikeLogDto) {
        upsertHikeCalls.add(dto)
    }

    override fun observePublicHikes(limit: Long): Flow<List<HikeLogDto>> = publicHikes

    override fun observeHikesByCreator(uid: String): Flow<List<HikeLogDto>> = publicHikes
}
