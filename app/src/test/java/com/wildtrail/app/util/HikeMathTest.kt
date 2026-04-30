package com.wildtrail.app.util

import com.wildtrail.app.domain.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-Kotlin unit tests for [HikeMath].
 *
 *  - No Android dependencies → run on the JVM, instant feedback in Android Studio.
 *  - Each test documents an expected value derived from a known reference
 *    (haversine of two well-known coordinates, or a hand-computed scenario).
 */
class HikeMathTest {

    /**
     * Distance between Milan and Rome should be ≈ 477 km (great-circle).
     * EXPECTED OUTCOME: a value within ±5 km of 477 km.
     */
    @Test
    fun `haversineMeters approximates the Milan-Rome distance`() {
        val milan = GeoPoint(lat = 45.4642, lng = 9.1900)
        val rome = GeoPoint(lat = 41.9028, lng = 12.4964)

        val km = HikeMath.haversineMeters(milan, rome) / 1000.0

        assertTrue("Got $km km, expected ~477", km in 470.0..485.0)
    }

    /**
     * Two identical points produce zero distance. EXPECTED: 0.0.
     */
    @Test
    fun `haversine of identical points is zero`() {
        val p = GeoPoint(lat = 0.0, lng = 0.0)
        assertEquals(0.0, HikeMath.haversineMeters(p, p), 0.0001)
    }

    /**
     * A two-point route should sum to the distance between them.
     * EXPECTED: positive km, equal to single-segment haversine / 1000.
     */
    @Test
    fun `totalDistanceKm of an empty or single-point route is zero`() {
        assertEquals(0f, HikeMath.totalDistanceKm(emptyList()), 0f)
        assertEquals(
            0f,
            HikeMath.totalDistanceKm(listOf(GeoPoint(45.4642, 9.1900))),
            0f,
        )
    }

    /**
     * Cumulative gain only counts positive altitude deltas.
     * EXPECTED: 100m + 50m = 150m for the sequence below.
     */
    @Test
    fun `elevationGainMeters counts only positive deltas`() {
        val points = listOf(
            GeoPoint(0.0, 0.0, altitudeM = 100.0),
            GeoPoint(0.0, 0.001, altitudeM = 200.0), // +100
            GeoPoint(0.0, 0.002, altitudeM = 150.0), // -50, ignored
            GeoPoint(0.0, 0.003, altitudeM = 200.0), // +50
        )
        assertEquals(150, HikeMath.elevationGainMeters(points))
    }

    /**
     * Average speed in km/h. 10 km in 1 hour = 10 km/h. EXPECTED: 10f.
     */
    @Test
    fun `avgSpeedKmh computes km per hour`() {
        assertEquals(10f, HikeMath.avgSpeedKmh(distanceKm = 10f, durationSeconds = 3_600L), 0.001f)
    }

    /**
     * Calories should be positive for any positive distance and increase
     * with elevation gain. EXPECTED: 800 + 50 = 850 for a 10 km hike with
     * 100 m of climbing.
     */
    @Test
    fun `estimateCalories scales with distance and elevation`() {
        val cals = HikeMath.estimateCalories(distanceKm = 10f, elevationGainM = 100)
        assertEquals(850, cals)
    }

    /**
     * XP grows with both distance and elevation, and a flat 5 XP completion
     * bonus is applied. EXPECTED: floor( 10*10 + 100/100*5 + 5 ) = 110.
     */
    @Test
    fun `xpFromHike awards distance + elevation + completion bonus`() {
        val xp = HikeMath.xpFromHike(distanceKm = 10f, elevationGainM = 100)
        assertEquals(110, xp)
    }

    /**
     * Even a tiny hike returns at least 10 XP (so users always feel rewarded).
     * EXPECTED: >= 10.
     */
    @Test
    fun `xpFromHike never returns less than ten`() {
        assertTrue(HikeMath.xpFromHike(distanceKm = 0.1f, elevationGainM = 0) >= 10)
    }
}
