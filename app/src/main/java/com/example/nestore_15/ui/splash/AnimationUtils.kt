package com.example.nestore_15.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Splash-specific motion utilities.
 *
 * Performance: all animations use [rememberInfiniteTransition] so they run on the composition
 * clock without recomposing the full tree each frame — only the animated values invalidate
 * the layers that read them via [graphicsLayer].
 */

/** Staggered phase (ms) keeps layers out of sync for a natural parallax feel. */
private fun layerPhase(layerIndex: Int): Int = layerIndex * 380

/**
 * Vertical float for collage cards — amplitude stays small (±8dp) to feel premium, not bouncy.
 */
@Composable
fun rememberSplashFloatY(layerIndex: Int, amplitudeDp: Float = 8f): Dp {
    val transition = rememberInfiniteTransition(label = "splashFloatY$layerIndex")
    val offset by transition.animateFloat(
        initialValue = -amplitudeDp,
        targetValue = amplitudeDp,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400 + layerPhase(layerIndex), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )
    return offset.dp
}

/**
 * Horizontal drift adds depth between foreground/background collage layers.
 */
@Composable
fun rememberSplashFloatX(layerIndex: Int, amplitudeDp: Float = 5f): Dp {
    val transition = rememberInfiniteTransition(label = "splashFloatX$layerIndex")
    val offset by transition.animateFloat(
        initialValue = -amplitudeDp,
        targetValue = amplitudeDp,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000 + layerPhase(layerIndex), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatX"
    )
    return offset.dp
}

/** Gentle breathing scale for title card and loader orb. */
@Composable
fun rememberSplashBreathScale(
    min: Float = 0.97f,
    max: Float = 1.03f,
    durationMs: Int = 2200
): Float {
    val transition = rememberInfiniteTransition(label = "splashBreath")
    return transition.animateFloat(
        initialValue = min,
        targetValue = max,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    ).value
}

/** Soft glow alpha pulse for the orange loader halo. */
@Composable
fun rememberGlowAlpha(min: Float = 0.35f, max: Float = 0.85f): Float {
    val transition = rememberInfiniteTransition(label = "splashGlow")
    return transition.animateFloat(
        initialValue = min,
        targetValue = max,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    ).value
}

/**
 * Entrance fade + slight scale — applied once when splash becomes visible.
 * Uses graphicsLayer to avoid layout passes during the animation.
 */
@Composable
fun Modifier.splashEntranceAlpha(alpha: Float): Modifier = this.graphicsLayer { this.alpha = alpha }

@Composable
fun Modifier.splashEntranceScale(scale: Float): Modifier = this.graphicsLayer {
    scaleX = scale
    scaleY = scale
}
