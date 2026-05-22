package com.wildtrail.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = ForestGreen40,
    onPrimary = OnForestGreenDark,
    primaryContainer = ForestGreen80,
    onPrimaryContainer = ForestGreen20,
    secondary = Moss40,
    onSecondary = OnForestGreenDark,
    secondaryContainer = Moss80,
    onSecondaryContainer = Moss20,
    tertiary = Sky40,
    background = Surface,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    error = ErrorLight,
    outline = Outline,
)

private val DarkColors = darkColorScheme(
    primary = ForestGreen80,
    onPrimary = ForestGreen20,
    primaryContainer = ForestGreen40,
    onPrimaryContainer = OnForestGreenDark,
    secondary = Moss80,
    onSecondary = Moss20,
    secondaryContainer = Moss40,
    onSecondaryContainer = OnForestGreenDark,
    tertiary = Sky80,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = ErrorDark,
    outline = OutlineDark,
)

/**
 * Top-level theme wrapper. We default to the WildTrail brand palette so
 * branded controls (Start hike, etc.) keep a consistent forest-green look
 * on every device. Material You / dynamic colour can be re-enabled by
 * passing `dynamicColor = true`.
 */
@Composable
fun WildTrailTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = WildTrailTypography,
        shapes = WildTrailShapes,
        content = content,
    )
}
