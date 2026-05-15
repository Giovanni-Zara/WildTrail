package com.wildtrail.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.wildtrail.app.domain.model.GeoPoint

/**
 * Lightweight, list-friendly preview of a hike's GPS route.
 *
 * Why not just embed a Google Map for every card?
 *  - Each [com.google.maps.android.compose.GoogleMap] allocates a fairly
 *    heavy SurfaceView; rendering 20+ in a LazyColumn drops frames hard
 *    on mid-range phones.
 *  - We don't actually need tiles for a thumbnail — the *shape* of the
 *    route is what tells you "this looks like a loop", "this is a
 *    summit-and-return", etc.
 *
 * So we draw the polyline ourselves on a [Canvas], normalising the
 * lat/lng range to fit the box with a small padding. Cheap, scrolls at
 * 60 fps even with many cards.
 */
@Composable
fun RouteThumbnail(
    points: List<GeoPoint>,
    modifier: Modifier = Modifier,
) {
    val brand = Color(0xFF2E5D3A)
    val backdrop = MaterialTheme.colorScheme.surfaceVariant

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backdrop),
    ) {
        if (points.size < 2) return@Canvas

        val padding = 8f
        val w = size.width - padding * 2
        val h = size.height - padding * 2

        val minLat = points.minOf { it.lat }
        val maxLat = points.maxOf { it.lat }
        val minLng = points.minOf { it.lng }
        val maxLng = points.maxOf { it.lng }

        // Guard against a degenerate bounding box (single point repeated).
        val latSpan = (maxLat - minLat).takeIf { it > 0.0 } ?: 1e-6
        val lngSpan = (maxLng - minLng).takeIf { it > 0.0 } ?: 1e-6

        fun project(p: GeoPoint): Offset {
            // Y axis inverted: higher lat → top of the canvas (smaller y).
            val x = padding + ((p.lng - minLng) / lngSpan).toFloat() * w
            val y = padding + (1f - ((p.lat - minLat) / latSpan).toFloat()) * h
            return Offset(x, y)
        }

        val path = Path().apply {
            val first = project(points.first())
            moveTo(first.x, first.y)
            for (i in 1 until points.size) {
                val p = project(points[i])
                lineTo(p.x, p.y)
            }
        }

        // Subtle shadow stroke under the main line for legibility on busy
        // card backgrounds.
        drawPath(
            path,
            color = Color.Black.copy(alpha = 0.15f),
            style = Stroke(width = 6f, cap = StrokeCap.Round),
        )
        drawPath(
            path,
            color = brand,
            style = Stroke(width = 4f, cap = StrokeCap.Round),
        )
    }
}
