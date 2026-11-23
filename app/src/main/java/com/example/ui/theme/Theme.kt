package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PrismDarkColorScheme =
  darkColorScheme(
    primary = PrismTokens.Colors.Amber,
    onPrimary = PrismTokens.Colors.Background,
    primaryContainer = PrismTokens.Colors.BoardRadialCenter,
    onPrimaryContainer = PrismTokens.Colors.AmberBright,
    secondary = PrismTokens.Colors.BeamBlue,
    onSecondary = PrismTokens.Colors.Background,
    secondaryContainer = PrismTokens.Colors.GlassSurface,
    onSecondaryContainer = PrismTokens.Colors.TextPrimary,
    tertiary = PrismTokens.Colors.Success,
    onTertiary = PrismTokens.Colors.Background,
    background = PrismTokens.Colors.Background,
    onBackground = PrismTokens.Colors.TextPrimary,
    surface = PrismTokens.Colors.BoardRadialCenter,
    onSurface = PrismTokens.Colors.TextPrimary,
    surfaceVariant = PrismTokens.Colors.GlassSurfaceElevated,
    onSurfaceVariant = PrismTokens.Colors.TextSecondary,
    outline = PrismTokens.Colors.GlassBorder,
    error = PrismTokens.Colors.Error,
    onError = PrismTokens.Colors.TextPrimary,
  )

private val PrismShapes =
  Shapes(
    small = PrismTokens.Radii.cardShape,
    medium = PrismTokens.Radii.cardShape,
    large = PrismTokens.Radii.modalShape,
  )

@Composable
fun PrismPathTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = PrismDarkColorScheme,
    typography = Typography,
    shapes = PrismShapes,
    content = content,
  )
}
