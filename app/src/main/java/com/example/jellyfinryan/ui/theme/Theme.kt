package com.example.jellyfinryan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider // If needed for custom CompositionLocals
// Change these imports:
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.lightColorScheme
// import androidx.compose.material3.darkColorScheme
import androidx.tv.material3.MaterialTheme // TV Material Theme
import androidx.tv.material3.darkColorScheme // TV dark color scheme
import androidx.tv.material3.lightColorScheme // TV light color scheme
import androidx.tv.material3.Typography // TV Typography (if you customize)

// Your Color.kt might need adjustment if it's defining M3 mobile colors.
// For TV, the palette might be slightly different or used differently.
// Example (ensure these colors are defined in your Color.kt or directly):
private val TVDarkColors = darkColorScheme( // Use androidx.tv.material3.darkColorScheme
    primary = Purple80, // from your Color.kt
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Neutral10, // Typical TV background
    surface = Neutral10,    // Cards and surfaces might be slightly lighter or different
    onPrimary = Purple20,
    onSecondary = PurpleGrey20,
    onTertiary = Pink20,
    onBackground = Neutral90,
    onSurface = Neutral90,
    onSurfaceVariant = NeutralGrey80 // For less prominent text/icons on surfaces
)

private val TVLightColors = lightColorScheme( // Use androidx.tv.material3.lightColorScheme
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Neutral99,
    surface = Neutral99,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = NeutralVariant10,
    onSurface = NeutralVariant10,
    onSurfaceVariant = NeutralGrey30
    // ... other colors
)

@Composable
fun JellyfinRyanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        TVDarkColors
    } else {
        TVLightColors
    }

    // Typography for TV usually needs to be larger.
    // You might want to define a specific TV Typography object.
    // For now, we'll use the default TV typography which is generally well-scaled.
    MaterialTheme( // This is now androidx.tv.material3.MaterialTheme
        colorScheme = colorScheme,
        typography = AppTypography, // Review AppTypography for TV suitability (larger fonts)
        // or use MaterialTheme.typography for default TV typography
        shapes = MaterialTheme.shapes, // Default TV shapes, or define your own
        content = content
    )
}