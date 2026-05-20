package com.example.nestore_15.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = FindAHomeColors.PrimaryDarkBlue,
    onPrimary = FindAHomeColors.TextOnPrimary,
    primaryContainer = FindAHomeColors.ImageBorder,
    onPrimaryContainer = FindAHomeColors.TextOnPrimary,
    secondary = FindAHomeColors.GreenAccent,
    onSecondary = FindAHomeColors.TextOnPrimary,
    tertiary = FindAHomeColors.OrangeAccent,
    onTertiary = FindAHomeColors.TextOnPrimary,
    background = FindAHomeColors.BackgroundSoft,
    onBackground = FindAHomeColors.PrimaryText,
    surface = FindAHomeColors.CardSurface,
    onSurface = FindAHomeColors.PrimaryText,
    surfaceVariant = FindAHomeColors.CardSurface,
    onSurfaceVariant = FindAHomeColors.TextSecondary,
    outline = FindAHomeColors.ImageBorder,
    error = FindAHomeColors.ErrorRed
)

/** Dark palette tuned for splash gradients and logo contrast. */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B6EDC),
    onPrimary = FindAHomeColors.TextOnPrimary,
    primaryContainer = Color(0xFF0D4779),
    onPrimaryContainer = FindAHomeColors.TextOnPrimary,
    secondary = FindAHomeColors.GreenAccent.copy(alpha = 0.85f),
    onSecondary = FindAHomeColors.TextOnPrimary,
    tertiary = FindAHomeColors.OrangeAccent.copy(alpha = 0.9f),
    onTertiary = FindAHomeColors.TextOnPrimary,
    background = Color(0xFF0A1628),
    onBackground = FindAHomeColors.TextOnPrimary,
    surface = Color(0xFF121E33),
    onSurface = FindAHomeColors.TextOnPrimary,
    surfaceVariant = Color(0xFF1C2E6B),
    onSurfaceVariant = FindAHomeColors.NeutralDot,
    outline = FindAHomeColors.ImageBorder,
    error = FindAHomeColors.ErrorRed
)

@Composable
fun FindAHomeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FindAHomeTypography,
        shapes = FindAHomeShapes,
        content = content
    )
}
