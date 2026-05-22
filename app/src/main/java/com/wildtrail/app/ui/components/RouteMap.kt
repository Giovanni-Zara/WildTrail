package com.wildtrail.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.wildtrail.app.domain.model.GeoPoint
import com.wildtrail.app.domain.model.HikeMediaItem
import com.wildtrail.app.domain.model.HikeMediaType

/**
 * Reusable Google Map that draws a hike route as a [Polyline].
 *
 *  - When [follow] is `true` (live tracking) the camera follows the most
 *    recent point. The polyline grows in real-time as new samples arrive.
 *  - When [follow] is `false` (post-hike viewer) the camera frames the
 *    full bounding box of the route exactly once.
 *  - [currentLocation] lets the map render *before* a route exists (e.g. the
 *    tracking screen before "Start hike"): we center on the user and drop a
 *    marker instead of showing a "waiting for GPS" box. The placeholder is
 *    only used when we have neither a route nor a current fix.
 */
@Composable
fun RouteMap(
    points: List<GeoPoint>,
    modifier: Modifier = Modifier,
    follow: Boolean = false,
    showCurrentMarker: Boolean = false,
    currentLocation: GeoPoint? = null,
    mediaItems: List<HikeMediaItem> = emptyList(),
) {
    if (points.isEmpty() && currentLocation == null) {
        // No route and no fix yet. We still take up the requested size so
        // the parent layout doesn't snap.
        androidx.compose.foundation.layout.Box(
            modifier = modifier,
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            Text(
                "Waiting for GPS…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val latLngs = remember(points) { points.map { LatLng(it.lat, it.lng) } }
    val currentLatLng = currentLocation?.let { LatLng(it.lat, it.lng) }
    val focus = latLngs.firstOrNull() ?: currentLatLng!!
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(focus, 16f)
    }

    if (latLngs.isEmpty()) {
        // Pre-route: just keep the camera on the user's latest fix.
        LaunchedEffect(currentLatLng) {
            currentLatLng?.let {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 16f))
            }
        }
    } else if (follow) {
        // Live-track: re-center on the most-recent point each emission.
        LaunchedEffect(latLngs.lastOrNull()) {
            latLngs.lastOrNull()?.let { newest ->
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(newest, 17f),
                )
            }
        }
    } else {
        // Post-hike: fit the whole route exactly once when the list grows
        // from empty → non-empty.
        LaunchedEffect(latLngs.size) {
            if (latLngs.size >= 2) {
                val bounds = LatLngBounds.Builder().apply {
                    latLngs.forEach { include(it) }
                }.build()
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, 80),
                )
            }
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = !follow,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
        ),
    ) {
        if (latLngs.isNotEmpty()) {
            Polyline(
                points = latLngs,
                color = androidx.compose.ui.graphics.Color(0xFF2E5D3A), // brand forest green
                width = 10f,
            )
        }
        if (showCurrentMarker) {
            (latLngs.lastOrNull() ?: currentLatLng)?.let { current ->
                Marker(state = MarkerState(position = current), title = "You")
            }
        }
        // Photo / audio note pins, colour-coded so it's obvious at a glance
        // which captured items are where on the route.
        mediaItems.forEach { media ->
            val hue = when (media.type) {
                HikeMediaType.PHOTO -> BitmapDescriptorFactory.HUE_AZURE
                HikeMediaType.AUDIO -> BitmapDescriptorFactory.HUE_ORANGE
            }
            Marker(
                state = MarkerState(position = LatLng(media.lat, media.lng)),
                title = when (media.type) {
                    HikeMediaType.PHOTO -> "Photo"
                    HikeMediaType.AUDIO -> "Voice note"
                },
                icon = BitmapDescriptorFactory.defaultMarker(hue),
            )
        }
    }
}
