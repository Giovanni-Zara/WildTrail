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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
 * Stateless card representing a single hike in a list.
 *
 * Shows:
 *  - Cover photo (if available) + title + description
 *  - Stats line: distance, duration, elevation
 *  - Community rating (avg stars + review count)
 *  - Trail characteristics chips (difficulty, water, …)
 *  - Like control (heart fills + count) — invokes [onLikeClick] which the
 *    parent wires to a ViewModel
 */
@Composable
fun HikeCard(
    hike: HikeLog,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLiked: Boolean = false,
    onLikeClick: (() -> Unit)? = null,
    likeCount: Int = hike.likesCount,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
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
            Text(text = hike.title, style = MaterialTheme.typography.titleMedium)

            hike.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Primary stats line
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Stat(Icons.Filled.Place, "%.1f km".format(hike.lengthKm))
                Stat(Icons.Filled.Timer, formatDuration(hike.durationSeconds))
                Stat(Icons.Filled.TrendingUp, "${hike.elevationGainMeters} m")
                Spacer(Modifier.weight(1f))
                LikeControl(
                    isLiked = isLiked,
                    count = likeCount,
                    onClick = onLikeClick,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Community rating
            if (hike.reviewCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StarRow(rating = hike.averageRating)
                    Text(
                        "  %.1f / 5  (%d)".format(hike.averageRating, hike.reviewCount),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            } else {
                Text(
                    "No reviews yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Characteristics row — small chips with the most informative bits.
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CharacteristicChip("Diff ${hike.difficultyLevel}/5")
                CharacteristicChip("Mud ${hike.mudRisk}/5")
                CharacteristicChip(
                    if (hike.waterAvailability) "💧 Water" else "🚱 No water",
                )
                CharacteristicChip(hike.surfaceType.label())
            }
        }
    }
}

@Composable
private fun LikeControl(isLiked: Boolean, count: Int, onClick: (() -> Unit)?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (onClick != null) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isLiked) "Unlike" else "Like",
                    tint = if (isLiked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text("$count", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CharacteristicChip(text: String) {
    androidx.compose.material3.AssistChip(
        onClick = { /* non-interactive */ },
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
    )
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

private fun SurfaceType.label() = when (this) {
    SurfaceType.MOUNTAIN -> "Mountain"
    SurfaceType.FOREST -> "Forest"
    SurfaceType.COASTAL -> "Coastal"
    SurfaceType.URBAN -> "Urban"
    SurfaceType.DESERT -> "Desert"
    SurfaceType.OTHER -> "Other"
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
                difficultyLevel = 4,
                mudRisk = 2,
                pathClarity = 5,
                fatigueLevel = 4,
                animalEncounterRisk = 1,
                waterAvailability = true,
                averageRating = 4.3f,
                reviewCount = 17,
            ),
            onClick = {},
            isLiked = true,
        )
    }
}
