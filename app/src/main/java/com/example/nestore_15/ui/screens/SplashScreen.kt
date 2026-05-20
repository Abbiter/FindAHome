package com.example.nestore_15.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nestore_15.R
import com.example.nestore_15.ui.components.PrimaryOrangeButton
import com.example.nestore_15.ui.splash.SplashAnimationConfig
import com.example.nestore_15.ui.theme.FindAHomeColors

@Composable
fun SplashScreen(
    isLoading: Boolean,
    errorMessage: String?,
    isNavigatingAway: Boolean,
    onExitAnimationFinished: () -> Unit,
    onRetry: () -> Unit,
    onSplashContentReady: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var taglineVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withFrameMillis { }
        onSplashContentReady()
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(SplashAnimationConfig.TAGLINE_DELAY_MS.toLong())
        taglineVisible = true
    }

    val screenAlpha by animateFloatAsState(
        targetValue = if (isNavigatingAway) 0f else 1f,
        animationSpec = tween(SplashAnimationConfig.EXIT_FADE_MS, easing = FastOutSlowInEasing),
        finishedListener = { value ->
            if (isNavigatingAway && value <= 0.01f) onExitAnimationFinished()
        },
        label = "splashExitAlpha"
    )

    val taglineAlpha by animateFloatAsState(
        targetValue = if (taglineVisible && !isNavigatingAway) 1f else 0f,
        animationSpec = tween(SplashAnimationConfig.TAGLINE_FADE_MS, easing = FastOutSlowInEasing),
        label = "taglineAlpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(FindAHomeColors.PrimaryDarkBlue)
            .graphicsLayer { alpha = screenAlpha }
    ) {
        val bottomAccentHeight = 3.dp
        val contentMaxHeight = maxHeight * 0.82f
        val logoMaxHeight = maxHeight * 0.42f

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .heightIn(max = contentMaxHeight)
                .padding(horizontal = 28.dp)
                .padding(bottom = bottomAccentHeight + 8.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.splash_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .heightIn(max = logoMaxHeight),
                contentScale = ContentScale.Fit
            )

            if (isLoading && errorMessage == null) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .size(44.dp),
                    color = FindAHomeColors.OrangeAccent,
                    strokeWidth = 4.dp
                )
            }

            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = FindAHomeColors.TextOnPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .graphicsLayer { alpha = taglineAlpha }
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomAccentHeight)
                .background(FindAHomeColors.OrangeAccent)
        )

        if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 32.dp, vertical = 24.dp)
                    .padding(bottom = bottomAccentHeight + 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.splash_connection_issue, errorMessage),
                    color = FindAHomeColors.TextOnPrimary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                PrimaryOrangeButton(
                    text = stringResource(R.string.splash_retry),
                    onClick = onRetry,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}
