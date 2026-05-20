package com.example.nestore_15.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp

/**
 * Ambient aurora-style background: layered soft radial color clouds that drift
 * independently on X/Y axes. No rotation, no spinning linear bands.
 *
 * Architecture (back → front):
 * 1. Base vertical gradient (deep brand navy)
 * 2. Slow base color morph (vertical shift)
 * 3–6. Four translucent radial blobs with parallax speeds
 * 7. Edge vignette for depth
 *
 * All layers render in a single [Canvas] pass for smooth 60fps drawing.
 */
@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    val scheme = MaterialTheme.colorScheme
    val palette = remember(scheme, darkTheme) {
        AuroraPalette.fromColorScheme(scheme, darkTheme)
    }

    val transition = rememberInfiniteTransition(label = "auroraAmbient")

    val baseMorph by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = auroraTween(SplashAnimationConfig.AURORA_BASE_MORPH_MS),
        label = "baseMorph"
    )

    // Blob 1 — drifts upper-left → center-right
    val b1x by transition.animateFloat(0.05f, 0.58f, auroraTween(SplashAnimationConfig.BLOB_1_X_MS), "b1x")
    val b1y by transition.animateFloat(0.08f, 0.42f, auroraTween(SplashAnimationConfig.BLOB_1_Y_MS), "b1y")
    val b1a by transition.animateFloat(
        SplashAnimationConfig.BLOB_1_ALPHA_MIN,
        SplashAnimationConfig.BLOB_1_ALPHA_MAX,
        auroraTween(SplashAnimationConfig.BLOB_1_ALPHA_MS),
        "b1a"
    )

    // Blob 2 — mid / right
    val b2x by transition.animateFloat(0.45f, 0.95f, auroraTween(SplashAnimationConfig.BLOB_2_X_MS), "b2x")
    val b2y by transition.animateFloat(0.22f, 0.62f, auroraTween(SplashAnimationConfig.BLOB_2_Y_MS), "b2y")
    val b2a by transition.animateFloat(
        SplashAnimationConfig.BLOB_2_ALPHA_MIN,
        SplashAnimationConfig.BLOB_2_ALPHA_MAX,
        auroraTween(SplashAnimationConfig.BLOB_2_ALPHA_MS),
        "b2a"
    )

    // Blob 3 — green, lower-left
    val b3x by transition.animateFloat(0.0f, 0.48f, auroraTween(SplashAnimationConfig.BLOB_3_X_MS), "b3x")
    val b3y by transition.animateFloat(0.52f, 0.92f, auroraTween(SplashAnimationConfig.BLOB_3_Y_MS), "b3y")
    val b3a by transition.animateFloat(
        SplashAnimationConfig.BLOB_3_ALPHA_MIN,
        SplashAnimationConfig.BLOB_3_ALPHA_MAX,
        auroraTween(SplashAnimationConfig.BLOB_3_ALPHA_MS),
        "b3a"
    )

    // Blob 4 — orange, lower-right (slowest parallax)
    val b4x by transition.animateFloat(0.38f, 0.88f, auroraTween(SplashAnimationConfig.BLOB_4_X_MS), "b4x")
    val b4y by transition.animateFloat(0.58f, 0.98f, auroraTween(SplashAnimationConfig.BLOB_4_Y_MS), "b4y")
    val b4a by transition.animateFloat(
        SplashAnimationConfig.BLOB_4_ALPHA_MIN,
        SplashAnimationConfig.BLOB_4_ALPHA_MAX,
        auroraTween(SplashAnimationConfig.BLOB_4_ALPHA_MS),
        "b4a"
    )

    val intensity = SplashAnimationConfig.AURORA_INTENSITY

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val maxDim = maxOf(w, h)

        val baseTop = lerp(palette.baseTop, palette.baseTopAlt, baseMorph * 0.35f)
        val baseBottom = lerp(palette.baseBottom, palette.baseBottomAlt, baseMorph * 0.25f)

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(baseTop, baseBottom),
                startY = 0f,
                endY = h
            )
        )

        drawSoftBlob(
            center = Offset(b1x * w, b1y * h),
            radius = maxDim * SplashAnimationConfig.BLOB_1_RADIUS_FRACTION,
            coreColor = palette.blobPrimary,
            alpha = b1a * intensity
        )
        drawSoftBlob(
            center = Offset(b2x * w, b2y * h),
            radius = maxDim * SplashAnimationConfig.BLOB_2_RADIUS_FRACTION,
            coreColor = palette.blobIndigo,
            alpha = b2a * intensity
        )
        drawSoftBlob(
            center = Offset(b3x * w, b3y * h),
            radius = maxDim * SplashAnimationConfig.BLOB_3_RADIUS_FRACTION,
            coreColor = palette.blobGreen,
            alpha = b3a * intensity
        )
        drawSoftBlob(
            center = Offset(b4x * w, b4y * h),
            radius = maxDim * SplashAnimationConfig.BLOB_4_RADIUS_FRACTION,
            coreColor = palette.blobOrange,
            alpha = b4a * intensity
        )

        // Subtle cinematic vignette — keeps focus on center logo
        drawRect(
            brush = Brush.radialGradient(
                0f to Color.Transparent,
                0.55f to Color.Transparent,
                1f to palette.vignette,
                center = Offset(w * 0.5f, h * 0.48f),
                radius = maxDim * 1.05f
            )
        )
    }
}

private fun auroraTween(durationMs: Int) = infiniteRepeatable<Float>(
    animation = tween(durationMs, easing = FastOutSlowInEasing),
    repeatMode = RepeatMode.Reverse
)

/**
 * Large gaussian-like radial wash: multiple alpha stops, no hard edges.
 */
private fun DrawScope.drawSoftBlob(
    center: Offset,
    radius: Float,
    coreColor: Color,
    alpha: Float
) {
    if (alpha <= 0.01f) return
    val a = alpha.coerceIn(0f, 1f)
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to coreColor.copy(alpha = a * 0.55f),
                0.28f to coreColor.copy(alpha = a * 0.32f),
                0.55f to coreColor.copy(alpha = a * 0.12f),
                0.78f to coreColor.copy(alpha = a * 0.04f),
                1f to Color.Transparent
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

/** Theme-derived colors for aurora layers (softened, translucent-ready). */
@Immutable
data class AuroraPalette(
    val baseTop: Color,
    val baseBottom: Color,
    val baseTopAlt: Color,
    val baseBottomAlt: Color,
    val blobPrimary: Color,
    val blobIndigo: Color,
    val blobGreen: Color,
    val blobOrange: Color,
    val vignette: Color
) {
    companion object {
        fun fromColorScheme(
            scheme: androidx.compose.material3.ColorScheme,
            darkTheme: Boolean
        ): AuroraPalette {
            return if (darkTheme) {
                AuroraPalette(
                    baseTop = scheme.background,
                    baseBottom = scheme.surface,
                    baseTopAlt = scheme.primary.copy(alpha = 0.35f),
                    baseBottomAlt = scheme.background,
                    blobPrimary = scheme.primary,
                    blobIndigo = scheme.primaryContainer,
                    blobGreen = scheme.secondary,
                    blobOrange = scheme.tertiary,
                    vignette = Color.Black.copy(alpha = 0.42f)
                )
            } else {
                AuroraPalette(
                    baseTop = scheme.primary,
                    baseBottom = scheme.primary.copy(alpha = 0.92f).let { c ->
                        Color(
                            red = c.red * 0.72f,
                            green = c.green * 0.78f,
                            blue = (c.blue * 1.05f).coerceIn(0f, 1f),
                            alpha = 1f
                        )
                    },
                    baseTopAlt = scheme.primaryContainer.copy(alpha = 0.55f),
                    baseBottomAlt = scheme.primary,
                    blobPrimary = scheme.primary,
                    blobIndigo = scheme.primaryContainer,
                    blobGreen = scheme.secondary,
                    blobOrange = scheme.tertiary,
                    vignette = scheme.primary.copy(alpha = 0.38f)
                )
            }
        }
    }
}
