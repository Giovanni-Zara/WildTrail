package com.wildtrail.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 shape tokens. We bias toward the larger, more "expressive" corner
 * radii of modern Material so cards, chips, sheets and buttons read soft and
 * contemporary — fitting the app's friendly, outdoorsy vibe.
 */
val WildTrailShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)
