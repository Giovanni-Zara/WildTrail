package com.wildtrail.app.util

import com.wildtrail.app.domain.model.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure-Kotlin helpers for deriving hike statistics from a list of GPS points.
 * No Android dependencies → trivially unit-testable.
 */
object HikeMath {

    private const val EARTH_RADIUS_M = 6_371_000.0

    /** Great-circle distance in metres (haversine). */
    fun haversineMeters(a: GeoPoint, b: GeoPoint): Double {
        val lat1 = Math.toRadians(a.lat)
        val lat2 = Math.toRadians(b.lat)
        val dLat = lat2 - lat1
        val dLng = Math.toRadians(b.lng - a.lng)
        val h = sin(dLat / 2).let { it * it } +
                cos(lat1) * cos(lat2) * sin(dLng / 2).let { it * it }
        val c = 2 * atan2(sqrt(h), sqrt(1 - h))
        return EARTH_RADIUS_M * c
    }

    /** Total length of the route in kilometres. */
    fun totalDistanceKm(points: List<GeoPoint>): Float {
        if (points.size < 2) return 0f
        var total = 0.0
        for (i in 1 until points.size) {
            total += haversineMeters(points[i - 1], points[i])
        }
        return (total / 1000.0).toFloat()
    }

    /** Cumulative positive elevation gain (metres). */
    fun elevationGainMeters(points: List<GeoPoint>): Int {
        if (points.size < 2) return 0
        var gain = 0.0
        for (i in 1 until points.size) {
            val prev = points[i - 1].altitudeM ?: continue
            val curr = points[i].altitudeM ?: continue
            val delta = curr - prev
            if (delta > 0) gain += delta
        }
        return gain.toInt()
    }

    /** Average speed (km/h) given route + duration. */
    fun avgSpeedKmh(distanceKm: Float, durationSeconds: Long): Float {
        if (durationSeconds <= 0L) return 0f
        val hours = durationSeconds / 3600.0
        return (distanceKm / hours).toFloat()
    }

    /**
     * Very rough calorie estimation: 80 kcal per km for a 70 kg adult on a
     * trail, scaled by elevation. Good enough for gamification — *not*
     * medical.
     */
    fun estimateCalories(distanceKm: Float, elevationGainM: Int): Int =
        (distanceKm * 80 + elevationGainM * 0.5f).toInt()

    /**
     * XP reward formula. Distance counts most, elevation gives a multiplier
     * (mountain hikes are much harder than flat ones), and a flat completion
     * bonus rewards just finishing a hike.
     */
    fun xpFromHike(distanceKm: Float, elevationGainM: Int): Int {
        val base = (distanceKm * 10f).toInt()
        val elevationBonus = (elevationGainM / 100f * 5f).toInt()
        return max(10, base + elevationBonus + 5)
    }
}
