package com.example.nestore_15.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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

@Composable
fun FindAHomeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Brand is light-first; ignore system dark for consistent marketplace look.
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = FindAHomeColors.PrimaryDarkBlue.toArgb()
            window.navigationBarColor = FindAHomeColors.BackgroundSoft.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = true
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
