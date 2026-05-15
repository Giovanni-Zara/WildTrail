package com.wildtrail.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** "Given star" gold, used by both read-only and editable rows. */
private val StarGold: Color = Color(0xFFF5B301)

/** "Ungiven star" grey — neutral but visible on light + dark surfaces. */
private val StarGrey: Color = Color(0xFFB9B9B9)

/**
 * A label + 1..5 chip row used everywhere we collect a non-star rating
 * (mud, fatigue, etc.).
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
 * Read-only star rating row.
 *
 * All five stars are always rendered as **filled** [Icons.Filled.Star]
 * shapes — the difference between "given" and "not given" is purely
 * colour: gold for given, grey for not given. This matches how 5-star
 * ratings work in mainstream apps (Maps, Play Store, Airbnb) and reads
 * better than mixing filled + outlined silhouettes.
 *
 * The number of gold stars is the rating *rounded to the nearest
 * integer* so it always matches the "N / 5" text shown next to it.
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
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (index < filled) StarGold else StarGrey,
                modifier = Modifier.size(size).padding(end = 1.dp),
            )
        }
    }
}

/**
 * Editable star row — used in the review form. Same colour convention as
 * [StarRow].
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
                    imageVector = Icons.Filled.Star,
                    contentDescription = "$i stars",
                    tint = if (i <= rating) StarGold else StarGrey,
                )
            }
        }
    }
}
