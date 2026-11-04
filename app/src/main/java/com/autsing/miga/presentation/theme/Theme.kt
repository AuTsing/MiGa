package com.autsing.miga.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

private val colorScheme = ColorScheme(
    primary = primary,
    primaryContainer = primaryContainer,
    onPrimary = onPrimary,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    secondaryContainer = secondaryContainer,
    onSecondary = onSecondary,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary,
    tertiaryContainer = tertiaryContainer,
    onTertiary = onTertiary,
    onTertiaryContainer = onTertiaryContainer,
    surfaceContainerLow = surfaceContainerLow,
    surfaceContainer = surfaceContainer,
    surfaceContainerHigh = surfaceContainerHigh,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,
    outline = outline,
    outlineVariant = outlineVariant,
    background = background,
    onBackground = onBackground,
    error = error,
    errorContainer = errorContainer,
    onError = onError,
    onErrorContainer = onErrorContainer,
)

@Composable
fun MiGaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
