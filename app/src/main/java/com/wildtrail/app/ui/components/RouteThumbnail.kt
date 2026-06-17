package com.wildtrail.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.wildtrail.app.domain.model.GeoPoint
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * List-friendly preview of a hike's GPS route, drawn over a **static map
 * background** for geographic context.
 *
 * It uses the Google Maps **lite mode**: the map renders as a single static
 * bitmap with no gesture handling, which is the approach Google recommends for
 * showing many maps in a scrolling list (a full interactive
 * [com.google.maps.android.compose.GoogleMap] allocates a heavy SurfaceView and
 * janks badly with 20+ in a LazyColumn). The interactive, zoomable map lives on
 * the full hike-detail screen ([RouteMap]); this preview is intentionally fixed.
 *
 * The camera (centre + zoom) is computed once from the route's bounding box and
 * the thumbnail's pixel size so the **entire segment is always framed** with a
 * little padding — never cropped, never interactive.
 */
@Composable
fun RouteThumbnail(
    points: List<GeoPoint>,
    modifier: Modifier = Modifier,
) {
    val brand = Color(0xFF2E5D3A)
    val backdrop = MaterialTheme.colorScheme.surfaceVariant

    BoxWithConstraints(modifier = modifier.clip(RoundedCornerShape(8.dp))) {
        val latLngs = remember(points) { points.map { LatLng(it.lat, it.lng) } }

        // Degenerate route (caller normally guards size >= 2): just show the
        // neutral backdrop rather than a misframed world map.
        if (latLngs.size < 2 || !constraints.hasBoundedWidth || !constraints.hasBoundedHeight) {
            Box(Modifier.fillMaxSize().background(backdrop))
            return@BoxWithConstraints
        }

        val widthPx = constraints.maxWidth
        val heightPx = constraints.maxHeight

        val camera = remember(latLngs, widthPx, heightPx) {
            val bounds = LatLngBounds.Builder().apply { latLngs.forEach { include(it) } }.build()
            CameraPosition.fromLatLngZoom(bounds.center, boundsZoom(bounds, widthPx, heightPx))
        }

        // Keyed on the computed camera so a recycled list slot reused for a
        // different hike always renders that hike's viewport, never a stale one.
        val cameraPositionState = remember(camera) { CameraPositionState(position = camera) }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            googleMapOptionsFactory = { GoogleMapOptions().liteMode(true) },
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = MapType.NORMAL),
            uiSettings = NonInteractiveUiSettings,
        ) {
            // White casing under the brand line keeps the route legible over
            // busy map tiles (the standard cartographic "route" treatment).
            Polyline(points = latLngs, color = Color.White, width = 10f)
            Polyline(points = latLngs, color = brand, width = 6f)
        }
    }
}

/** Every gesture and control disabled — a fixed, non-interactive map. */
private val NonInteractiveUiSettings = MapUiSettings(
    compassEnabled = false,
    indoorLevelPickerEnabled = false,
    mapToolbarEnabled = false,
    myLocationButtonEnabled = false,
    rotationGesturesEnabled = false,
    scrollGesturesEnabled = false,
    scrollGesturesEnabledDuringRotateOrZoom = false,
    tiltGesturesEnabled = false,
    zoomControlsEnabled = false,
    zoomGesturesEnabled = false,
)

private const val WORLD_PX = 256.0
private const val MAX_ZOOM = 19.0

/** Leaves ~22% margin so the route sits comfortably inside the frame. */
private const val PADDING_FACTOR = 0.78

/**
 * Smallest zoom at which [bounds] fits inside a [widthPx] × [heightPx] viewport,
 * using the standard Web-Mercator "fit bounds" derivation. Returns a fixed
 * zoom suitable for a non-interactive snapshot.
 */
private fun boundsZoom(bounds: LatLngBounds, widthPx: Int, heightPx: Int): Float {
    val ne = bounds.northeast
    val sw = bounds.southwest

    val latFraction = (latRad(ne.latitude) - latRad(sw.latitude)) / PI
    val lngDiff = ne.longitude - sw.longitude
    val lngFraction = (if (lngDiff < 0) lngDiff + 360.0 else lngDiff) / 360.0

    // A span of ~0 in one axis (single point, or a perfectly straight N-S/E-W
    // trace) would blow up the log; fall back to the max zoom for that axis.
    val latZoom = if (latFraction <= 0.0) MAX_ZOOM else zoomFor(heightPx * PADDING_FACTOR, latFraction)
    val lngZoom = if (lngFraction <= 0.0) MAX_ZOOM else zoomFor(widthPx * PADDING_FACTOR, lngFraction)

    return minOf(latZoom, lngZoom, MAX_ZOOM).coerceIn(2.0, MAX_ZOOM).toFloat()
}

private fun zoomFor(mapPx: Double, fraction: Double): Double =
    ln(mapPx / WORLD_PX / fraction) / ln(2.0)

private fun latRad(latDeg: Double): Double {
    val s = sin(latDeg * PI / 180.0)
    val radX2 = ln((1 + s) / (1 - s)) / 2.0
    return max(min(radX2, PI), -PI) / 2.0
}
