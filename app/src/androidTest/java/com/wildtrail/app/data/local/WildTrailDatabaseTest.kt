package com.wildtrail.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wildtrail.app.data.local.entity.HikeLogEntity
import com.wildtrail.app.data.local.entity.UserEntity
import com.wildtrail.app.domain.model.GeoPoint
import com.wildtrail.app.domain.model.SurfaceType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the Room database. Run with:
 *   ./gradlew :app:connectedDebugAndroidTest
 *
 * The DB is created in-memory via [Room.inMemoryDatabaseBuilder] so each test
 * gets a fresh DB and nothing is written to disk.
 */
@RunWith(AndroidJUnit4::class)
class WildTrailDatabaseTest {

    private lateinit var db: WildTrailDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, WildTrailDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = db.close()

    /**
     * EXPECTED: a UserEntity inserted with upsert can be read back identically.
     */
    @Test
    fun userDao_upsertAndRead() = runBlocking {
        val dao = db.userDao()
        val u = UserEntity(
            firebaseUid = "uid-1",
            username = "giovanni",
            age = 28,
            country = "IT",
            level = 2,
            xpPoints = 120,
            totalDistanceKm = 14.2f,
            totalHikesCount = 3,
            profilePictureUrl = null,
            bio = null,
            createdAt = 1L,
            lastActive = 2L,
            isPublic = true,
        )
        dao.upsert(u)

        val read = dao.getById("uid-1")
        assertEquals(u, read)
    }

    /**
     * EXPECTED: HikeLogEntity persists routeCoordinates correctly via the
     *           Converters (round-trip JSON serialisation).
     */
    @Test
    fun hikeLogDao_persistsRoute() = runBlocking {
        val userDao = db.userDao()
        val hikeDao = db.hikeLogDao()

        userDao.upsert(
            UserEntity(
                firebaseUid = "uid-1", username = "g", age = null, country = null,
                level = 1, xpPoints = 0, totalDistanceKm = 0f, totalHikesCount = 0,
                profilePictureUrl = null, bio = null, createdAt = 0L, lastActive = 0L,
                isPublic = true,
            ),
        )

        val route = listOf(
            GeoPoint(45.0, 9.0, altitudeM = 100.0, timestamp = 1L),
            GeoPoint(45.001, 9.001, altitudeM = 110.0, timestamp = 2L),
        )
        val hike = HikeLogEntity(
            hikeId = "h1",
            creatorFirebaseUid = "uid-1",
            workoutId = null,
            title = "Test hike",
            description = null,
            avgSpeedKmh = 4.5f,
            stepCount = 0,
            caloriesBurned = 100,
            coverPhotoUrl = null,
            xpEarned = 10,
            likesCount = 0,
            surfaceType = SurfaceType.MOUNTAIN,
            lengthKm = 0.1f,
            durationSeconds = 300L,
            startedAt = 0L,
            endedAt = 0L,
            elevationGainMeters = 10,
            routeCoordinates = route,
            isPrivate = false,
        )

        hikeDao.upsert(hike)
        val read = hikeDao.getById("h1")
        assertNotNull(read)
        assertEquals(route, read!!.routeCoordinates)
    }

    /**
     * EXPECTED: deleting the parent UserEntity cascades and removes the
     *           HikeLogEntity (FK ON DELETE CASCADE).
     */
    @Test
    fun deletingUser_cascadesToHikes() = runBlocking {
        val userDao = db.userDao()
        val hikeDao = db.hikeLogDao()

        userDao.upsert(
            UserEntity(
                firebaseUid = "uid-1", username = "g", age = null, country = null,
                level = 1, xpPoints = 0, totalDistanceKm = 0f, totalHikesCount = 0,
                profilePictureUrl = null, bio = null, createdAt = 0L, lastActive = 0L,
                isPublic = true,
            ),
        )
        hikeDao.upsert(
            HikeLogEntity(
                hikeId = "h1", creatorFirebaseUid = "uid-1", workoutId = null,
                title = "x", description = null, avgSpeedKmh = 0f, stepCount = 0,
                caloriesBurned = 0, coverPhotoUrl = null, xpEarned = 0, likesCount = 0,
                surfaceType = SurfaceType.OTHER, lengthKm = 0f, durationSeconds = 0,
                startedAt = 0, endedAt = 0, elevationGainMeters = 0,
                routeCoordinates = emptyList(), isPrivate = false,
            ),
        )

        userDao.deleteById("uid-1")

        assertNull(hikeDao.getById("h1"))
    }

    /**
     * EXPECTED: observePublicFeed emits an empty list initially, then
     *           updates to contain the inserted public hike.
     */
    @Test
    fun publicFeed_observesNewHikes() = runBlocking {
        val userDao = db.userDao()
        val hikeDao = db.hikeLogDao()

        userDao.upsert(
            UserEntity(
                firebaseUid = "uid-1", username = "g", age = null, country = null,
                level = 1, xpPoints = 0, totalDistanceKm = 0f, totalHikesCount = 0,
                profilePictureUrl = null, bio = null, createdAt = 0L, lastActive = 0L,
                isPublic = true,
            ),
        )

        // Insert a public hike.
        hikeDao.upsert(
            HikeLogEntity(
                hikeId = "h1", creatorFirebaseUid = "uid-1", workoutId = null,
                title = "public", description = null, avgSpeedKmh = 0f, stepCount = 0,
                caloriesBurned = 0, coverPhotoUrl = null, xpEarned = 0, likesCount = 0,
                surfaceType = SurfaceType.OTHER, lengthKm = 0f, durationSeconds = 0,
                startedAt = 0, endedAt = 0, elevationGainMeters = 0,
                routeCoordinates = emptyList(), isPrivate = false,
            ),
        )

        val feed = hikeDao.observePublicFeed(50).first()
        assertEquals(1, feed.size)
        assertEquals("h1", feed.first().hikeId)
    }
}
