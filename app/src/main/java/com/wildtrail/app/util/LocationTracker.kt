package com.wildtrail.app.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.wildtrail.app.domain.model.GeoPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * GPS sampler used by the tracking screen / ViewModel.
 *
 * The fused-location provider chooses the best source automatically (GPS,
 * Wi-Fi, cell). We request HIGH_ACCURACY because hikes need precise traces;
 * for an idle screen you'd downgrade to BALANCED to save battery (one of
 * the energy-efficiency considerations called out in the assignment).
 *
 * Permission handling is **not** done here — callers must request the
 * runtime permissions first. We expose a clean Flow of [GeoPoint] so the
 * rest of the app stays platform-agnostic.
 */
class LocationTracker(private val context: Context) {

    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Cold flow that emits a [GeoPoint] every [intervalMs]. Cancels the
     * underlying callback when the collector goes away.
     *
     * Throws [SecurityException] if permission has been revoked at runtime —
     * we suppress the lint check because we DO check it in [hasLocationPermission].
     */
    @SuppressLint("MissingPermission")
    fun observeLocation(intervalMs: Long = 2_000L): Flow<GeoPoint> = callbackFlow {
        if (!hasLocationPermission()) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { trySend(it.toGeoPoint()) }
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { client.removeLocationUpdates(callback) }
    }

    private fun Location.toGeoPoint(): GeoPoint = GeoPoint(
        lat = latitude,
        lng = longitude,
        altitudeM = if (hasAltitude()) altitude else null,
        timestamp = time,
    )
}
