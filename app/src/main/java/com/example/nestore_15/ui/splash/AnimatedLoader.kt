package com.example.nestore_15.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nestore_15.ui.theme.FindAHomeColors

/**
 * Premium splash loader: pulsing orange orb with soft glow + animated dots.
 *
 * Layout choice: sits in the collage focal point so loading feels integrated, not blocking.
 * Performance: single infinite transition drives orb scale, glow, and dot offsets together.
 */
@Composable
fun AnimatedLoader(
    visible: Boolean,
    modifier: Modifier = Modifier,
    orbSize: Dp = 56.dp
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(250))
    ) {
        val transition = rememberInfiniteTransition(label = "loader")
        val orbScale by transition.animateFloat(
            initialValue = 0.88f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(1100, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orbScale"
        )
        val glowAlpha = rememberGlowAlpha()

        Box(contentAlignment = Alignment.Center) {
            // Outer glow halo — radial gradient, no extra layouts
            Box(
                Modifier
                    .size(orbSize * 2.2f)
                    .scale(orbScale * 1.05f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                FindAHomeColors.OrangeAccent.copy(alpha = glowAlpha * 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )
            // Core orb
            Box(
                Modifier
                    .size(orbSize)
                    .scale(orbScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                FindAHomeColors.OrangeAccent,
                                FindAHomeColors.OrangeAccent.copy(alpha = 0.75f)
                            )
                        )
                    )
            )
            // Three animated dots below the orb (offset so they don't cover the core)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = orbSize * 0.75f)
            ) {
                repeat(3) { index ->
                    val dotOffset by transition.animateFloat(
                        initialValue = 0f,
                        targetValue = -6f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500 + index * 120),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot$index"
                    )
                    Box(
                        Modifier
                            .size(8.dp)
                            .scale(1f + dotOffset * -0.04f)
                            .clip(CircleShape)
                            .background(FindAHomeColors.TextOnPrimary.copy(alpha = 0.9f))
                    )
                }
            }
        }
    }
}
