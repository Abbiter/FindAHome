package com.example.nestore_15.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nestore_15.R
import com.example.nestore_15.ui.theme.FindAHomeColors

/**
 * Centered brand logo with one-shot entrance and subtle ongoing motion.
 * Uses [graphicsLayer] for scale/alpha/offset to avoid extra layout passes.
 */
@Composable
fun SplashLogoContent(
    maxLogoHeight: Dp,
    modifier: Modifier = Modifier,
    widthFraction: Float = 0.72f
) {
    // Entrance runs while the system splash is still visible (held), so the handoff is already animated.
    val entranceAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(SplashAnimationConfig.LOGO_ENTRANCE_MS, easing = FastOutSlowInEasing),
        label = "logoEntranceAlpha"
    )
    val entranceScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(SplashAnimationConfig.LOGO_ENTRANCE_MS, easing = FastOutSlowInEasing),
        label = "logoEntranceScale"
    )

    val floatTransition = rememberInfiniteTransition(label = "logoFloat")
    val floatY by floatTransition.animateFloat(
        initialValue = -SplashAnimationConfig.LOGO_FLOAT_AMPLITUDE_DP,
        targetValue = SplashAnimationConfig.LOGO_FLOAT_AMPLITUDE_DP,
        animationSpec = infiniteRepeatable(
            animation = tween(SplashAnimationConfig.LOGO_FLOAT_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoFloatY"
    )
    val breathScale = rememberSplashBreathScale(
        min = SplashAnimationConfig.LOGO_BREATH_MIN,
        max = SplashAnimationConfig.LOGO_BREATH_MAX,
        durationMs = SplashAnimationConfig.LOGO_BREATH_MS
    )
    val glowAlpha = rememberGlowAlpha(
        min = SplashAnimationConfig.LOGO_GLOW_MIN_ALPHA,
        max = SplashAnimationConfig.LOGO_GLOW_MAX_ALPHA
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(maxLogoHeight * 1.2f)
                .graphicsLayer {
                    alpha = glowAlpha * entranceAlpha
                    scaleX = breathScale * 1.06f
                    scaleY = breathScale * 1.06f
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            FindAHomeColors.OrangeAccent.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        Image(
            painter = painterResource(R.drawable.splash_logo),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .heightIn(max = maxLogoHeight)
                .offset(y = floatY.dp)
                .graphicsLayer {
                    alpha = entranceAlpha
                    scaleX = entranceScale * breathScale
                    scaleY = entranceScale * breathScale
                },
            contentScale = ContentScale.Fit
        )
    }
}
