package com.wildtrail.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.wildtrail.app.domain.model.HikeLog
import com.wildtrail.app.domain.model.SurfaceType
import com.wildtrail.app.ui.theme.WildTrailTheme

/**
 * Stateless card representing a single hike in a list. Receives the hike +
 * a click callback, owns no state of its own.
 *
 *  - Renders the cover photo via Coil (placeholder if null).
 *  - Shows distance, duration, likes — the three numbers a hiker glances at.
 */
@Composable
fun HikeCard(
    hike: HikeLog,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (hike.coverPhotoUrl != null) {
                AsyncImage(
                    model = hike.coverPhotoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                )
                Spacer(Modifier.height(12.dp))
            }
            Text(
                text = hike.title,
                style = MaterialTheme.typography.titleMedium,
            )
            hike.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Stat(Icons.Filled.Place, "%.1f km".format(hike.lengthKm))
                Stat(Icons.Filled.Timer, formatDuration(hike.durationSeconds))
                Stat(Icons.Filled.Favorite, hike.likesCount.toString())
            }
        }
    }
}

@Composable
private fun Stat(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

@Preview(showBackground = true)
@Composable
private fun HikeCardPreview() {
    WildTrailTheme {
        HikeCard(
            hike = HikeLog(
                hikeId = "1",
                creatorFirebaseUid = "u1",
                workoutId = null,
                title = "Sunrise on Monte Bianco",
                description = "Cold, hard, glorious.",
                avgSpeedKmh = 4.2f,
                stepCount = 12_000,
                caloriesBurned = 1_400,
                coverPhotoUrl = null,
                xpEarned = 220,
                likesCount = 14,
                surfaceType = SurfaceType.MOUNTAIN,
                lengthKm = 8.4f,
                durationSeconds = 9_840,
                startedAt = 0L,
                endedAt = 0L,
                elevationGainMeters = 1_120,
                routeCoordinates = emptyList(),
                isPrivate = false,
            ),
            onClick = {},
        )
    }
}
