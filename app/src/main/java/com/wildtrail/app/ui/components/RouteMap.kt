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
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.wildtrail.app.domain.model.GeoPoint

/**
 * Reusable Google Map that draws a hike route as a [Polyline].
 *
 *  - When [follow] is `true` (live tracking) the camera follows the most
 *    recent point. The polyline grows in real-time as new samples arrive.
 *  - When [follow] is `false` (post-hike viewer) the camera frames the
 *    full bounding box of the route exactly once.
 *  - When [points] is empty we render a small placeholder so the parent
 *    can give the map a fixed height without an awkward empty box.
 */
@Composable
fun RouteMap(
    points: List<GeoPoint>,
    modifier: Modifier = Modifier,
    follow: Boolean = false,
    showCurrentMarker: Boolean = false,
) {
    if (points.isEmpty()) {
        // Empty placeholder. We still take up the requested size so the
        // parent layout doesn't snap.
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
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latLngs.first(), 16f)
    }

    // Live-track: re-center on the most-recent point each emission.
    if (follow) {
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
        Polyline(
            points = latLngs,
            color = androidx.compose.ui.graphics.Color(0xFF2E5D3A), // brand forest green
            width = 10f,
        )
        if (showCurrentMarker) {
            latLngs.lastOrNull()?.let { current ->
                Marker(state = MarkerState(position = current), title = "You")
            }
        }
    }
}
