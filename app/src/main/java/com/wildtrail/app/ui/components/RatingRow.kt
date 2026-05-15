package com.wildtrail.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * A label + 1..5 chip row used everywhere we collect a rating from the
 * user. Stateless: takes the current value + a callback.
 */
@Composable
fun RatingRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 1..5,
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        ) {
            range.forEach { i ->
                FilterChip(
                    selected = i == value,
                    onClick = { onValueChange(i) },
                    label = { Text("$i") },
                )
            }
        }
    }
}

/**
 * Read-only star rating row — used to render an `overallRating` (1..5).
 *
 * The number of filled stars is the rating *rounded to the nearest integer*
 * so it always matches the "N / 5" text shown next to it (e.g. a 4 shows
 * four solid stars and one outlined star, not a truncated count).
 */
@Composable
fun StarRow(
    rating: Float,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 18.dp,
) {
    val filled = rating.roundToInt().coerceIn(0, 5)
    Row(modifier = modifier) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < filled) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size).padding(end = 1.dp),
            )
        }
    }
}

/**
 * Editable star row — used in the review form.
 */
@Composable
fun EditableStarRow(
    rating: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..5) {
            IconButton(onClick = { onChange(i) }) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "$i stars",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
