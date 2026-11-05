package com.autsing.miga.tile

import androidx.compose.ui.graphics.toArgb
import androidx.wear.protolayout.material3.ColorScheme
import androidx.wear.protolayout.types.argb
import com.autsing.miga.presentation.theme.error
import com.autsing.miga.presentation.theme.errorContainer
import com.autsing.miga.presentation.theme.onBackground
import com.autsing.miga.presentation.theme.onError
import com.autsing.miga.presentation.theme.onErrorContainer
import com.autsing.miga.presentation.theme.onPrimary
import com.autsing.miga.presentation.theme.onPrimaryContainer
import com.autsing.miga.presentation.theme.onSecondary
import com.autsing.miga.presentation.theme.onSecondaryContainer
import com.autsing.miga.presentation.theme.onSurface
import com.autsing.miga.presentation.theme.onSurfaceVariant
import com.autsing.miga.presentation.theme.onTertiary
import com.autsing.miga.presentation.theme.onTertiaryContainer
import com.autsing.miga.presentation.theme.outline
import com.autsing.miga.presentation.theme.outlineVariant
import com.autsing.miga.presentation.theme.primary
import com.autsing.miga.presentation.theme.primaryContainer
import com.autsing.miga.presentation.theme.secondary
import com.autsing.miga.presentation.theme.secondaryContainer
import com.autsing.miga.presentation.theme.surfaceContainer
import com.autsing.miga.presentation.theme.surfaceContainerHigh
import com.autsing.miga.presentation.theme.surfaceContainerLow
import com.autsing.miga.presentation.theme.tertiary
import com.autsing.miga.presentation.theme.tertiaryContainer

val colorScheme = ColorScheme(
    primary = primary.toArgb().argb,
    primaryContainer = primaryContainer.toArgb().argb,
    onPrimary = onPrimary.toArgb().argb,
    onPrimaryContainer = onPrimaryContainer.toArgb().argb,
    secondary = secondary.toArgb().argb,
    secondaryContainer = secondaryContainer.toArgb().argb,
    onSecondary = onSecondary.toArgb().argb,
    onSecondaryContainer = onSecondaryContainer.toArgb().argb,
    tertiary = tertiary.toArgb().argb,
    tertiaryContainer = tertiaryContainer.toArgb().argb,
    onTertiary = onTertiary.toArgb().argb,
    onTertiaryContainer = onTertiaryContainer.toArgb().argb,
    surfaceContainerLow = surfaceContainerLow.toArgb().argb,
    surfaceContainer = surfaceContainer.toArgb().argb,
    surfaceContainerHigh = surfaceContainerHigh.toArgb().argb,
    onSurface = onSurface.toArgb().argb,
    onSurfaceVariant = onSurfaceVariant.toArgb().argb,
    outline = outline.toArgb().argb,
    outlineVariant = outlineVariant.toArgb().argb,
    onBackground = onBackground.toArgb().argb,
    error = error.toArgb().argb,
    errorContainer = errorContainer.toArgb().argb,
    onError = onError.toArgb().argb,
    onErrorContainer = onErrorContainer.toArgb().argb,
)
