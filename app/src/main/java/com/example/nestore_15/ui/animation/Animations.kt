package com.example.nestore_15.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** Gentle vertical float for splash collage images. */
@Composable
fun rememberFloatOffset(index: Int): Float {
    val transition = rememberInfiniteTransition(label = "float")
    val phase = index * 400
    return transition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200 + phase, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    ).value
}

/** Pulsing scale for loading indicators. */
@Composable
fun rememberPulseScale(): Float {
    val transition = rememberInfiniteTransition(label = "pulse")
    return transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    ).value
}

@Composable
fun Modifier.fadeInOnAppear(visible: Boolean): Modifier {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "fade"
    )
    return this.graphicsLayer { this.alpha = alpha }
}

@Composable
fun Modifier.pressScale(pressed: Boolean): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(120),
        label = "press"
    )
    return this.scale(scale)
}

@Composable
fun FadeSlideIn(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(450)) + slideInVertically(
            initialOffsetY = { it / 8 },
            animationSpec = tween(450, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(tween(250))
    ) {
        content()
    }
}

@Composable
fun Modifier.listItemAnimation(index: Int): Modifier {
    val offsetY by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(400 + index * 40, easing = FastOutSlowInEasing),
        label = "listItem"
    )
    return this.offset(y = offsetY.dp)
}
