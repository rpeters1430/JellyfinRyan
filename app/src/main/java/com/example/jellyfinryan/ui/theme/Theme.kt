package com.example.jellyfinryan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.*
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.lightColorScheme

private val DarkExpressiveColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = OnBluePrimary,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = OnBluePrimaryContainer,
    secondary = PurpleSecondary,
    onSecondary = OnPurpleSecondary,
    secondaryContainer = PurpleSecondaryContainer,
    onSecondaryContainer = OnPurpleSecondaryContainer,
    tertiary = GreenTertiary,
    onTertiary = OnGreenTertiary,
    tertiaryContainer = GreenTertiaryContainer,
    onTertiaryContainer = OnGreenTertiaryContainer,
    background = DarkBackground,
    onBackground = OnDarkBackground,
    surface = DarkSurface,
    onSurface = OnDarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSurfaceVariant,
    error = Error,
    onError = OnError
)

private val LightExpressiveColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = OnBluePrimary,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = OnBluePrimaryContainer,
    secondary = PurpleSecondary,
    onSecondary = OnPurpleSecondary,
    secondaryContainer = PurpleSecondaryContainer,
    onSecondaryContainer = OnPurpleSecondaryContainer,
    tertiary = GreenTertiary,
    onTertiary = OnGreenTertiary,
    tertiaryContainer = GreenTertiaryContainer,
    onTertiaryContainer = OnGreenTertiaryContainer,
    background = LightBackground,
    onBackground = OnLightBackground,
    surface = LightSurface,
    onSurface = OnLightSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = OnLightSurfaceVariant,
    error = Error,
    onError = OnError
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun JellyfinTVTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkExpressiveColorScheme
    } else {
        LightExpressiveColorScheme
    }

    androidx.tv.material3.MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}