package com.example.nestore_15.ui.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fluid, looping gradient background driven by Material 3 theme colors.
 *
 * Animation is isolated here so only this layer recomposes each frame.
 * Uses three out-of-phase oscillators (Reverse) to avoid obvious loop resets.
 */
@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    val scheme = MaterialTheme.colorScheme
    val palette = remember(scheme, darkTheme) {
        SplashGradientPalette.fromColorScheme(scheme, darkTheme)
    }

    val transition = rememberInfiniteTransition(label = "splashGradient")

    val phase1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(SplashAnimationConfig.GRADIENT_PHASE_1_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase1"
    )
    val phase2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(SplashAnimationConfig.GRADIENT_PHASE_2_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase2"
    )
    val phase3 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(SplashAnimationConfig.GRADIENT_PHASE_3_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase3"
    )

    BoxWithConstraints(modifier = modifier) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        val driftAngle = (phase1 * 0.55f + phase2 * 0.35f) * (Math.PI * 2).toFloat()
        val morph = phase3

        val start = Offset(
            x = width * (0.15f + 0.35f * cos(driftAngle)),
            y = height * (0.1f + 0.3f * sin(driftAngle * 0.85f))
        )
        val end = Offset(
            x = width * (0.85f - 0.35f * cos(driftAngle + 1.1f)),
            y = height * (0.9f - 0.3f * sin(driftAngle * 0.85f + 0.6f))
        )

        val midBlend = lerp(palette.mid, palette.highlight, morph * 0.4f + phase1 * 0.15f)
        val gradientColors = listOf(
            lerp(palette.deep, palette.primary, phase2 * 0.25f),
            lerp(palette.primary, midBlend, phase1 * 0.3f),
            lerp(midBlend, palette.accentSecondary, morph * 0.35f),
            lerp(palette.accentSecondary, palette.accentTertiary, phase2 * 0.2f),
            lerp(palette.accentTertiary, palette.deep, phase3 * 0.18f)
        )

        val stop0 = 0f
        val stop1 = 0.22f + phase3 * 0.08f
        val stop2 = 0.48f + phase1 * 0.06f
        val stop3 = 0.72f + phase2 * 0.05f
        val stop4 = 1f

        Box(
            Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.linearGradient(
                            colorStops = arrayOf(
                                stop0 to gradientColors[0],
                                stop1 to gradientColors[1],
                                stop2 to gradientColors[2],
                                stop3 to gradientColors[3],
                                stop4 to gradientColors[4]
                            ),
                            start = start,
                            end = end
                        )
                    )
                }
        )
    }
}

/** Resolved gradient stops derived from Material 3 [androidx.compose.material3.ColorScheme]. */
@Immutable
data class SplashGradientPalette(
    val deep: Color,
    val primary: Color,
    val mid: Color,
    val highlight: Color,
    val accentSecondary: Color,
    val accentTertiary: Color
) {
    companion object {
        fun fromColorScheme(
            scheme: androidx.compose.material3.ColorScheme,
            darkTheme: Boolean
        ): SplashGradientPalette {
            return if (darkTheme) {
                SplashGradientPalette(
                    deep = scheme.background,
                    primary = scheme.primary.copy(alpha = 0.95f),
                    mid = scheme.primaryContainer,
                    highlight = scheme.primary.copy(alpha = 0.72f),
                    accentSecondary = scheme.secondary.copy(alpha = 0.55f),
                    accentTertiary = scheme.tertiary.copy(alpha = 0.45f)
                )
            } else {
                SplashGradientPalette(
                    deep = scheme.primary,
                    primary = scheme.primary,
                    mid = scheme.primaryContainer,
                    highlight = scheme.primaryContainer.copy(alpha = 0.85f),
                    accentSecondary = scheme.secondary.copy(alpha = 0.38f),
                    accentTertiary = scheme.tertiary.copy(alpha = 0.32f)
                )
            }
        }
    }
}
