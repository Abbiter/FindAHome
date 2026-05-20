package com.example.nestore_15.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nestore_15.R
import com.example.nestore_15.ui.components.PrimaryOrangeButton
import com.example.nestore_15.ui.splash.AnimatedLoader
import com.example.nestore_15.ui.animation.FadeSlideIn
import com.example.nestore_15.ui.splash.ImageCollage
import com.example.nestore_15.ui.splash.rememberSplashBreathScale
import com.example.nestore_15.ui.splash.rememberSplashFloatY
import com.example.nestore_15.ui.splash.splashEntranceAlpha
import com.example.nestore_15.ui.splash.splashEntranceScale
import com.example.nestore_15.ui.theme.FindAHomeColors
import com.example.nestore_15.ui.theme.SplashCardShape

/**
 * Premium animated splash for Find a Home.
 *
 * Structure: title card (top) → photo collage + loader (center) → tagline card (bottom).
 * Entrance uses a single fade/scale pass; ongoing motion is isolated to splash animation utils.
 */
@Composable
fun SplashScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var entranceStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entranceStarted = true }

    val entranceAlpha by animateFloatAsState(
        targetValue = if (entranceStarted) 1f else 0f,
        animationSpec = tween(750),
        label = "splashEntranceAlpha"
    )
    val entranceScale by animateFloatAsState(
        targetValue = if (entranceStarted) 1f else 0.94f,
        animationSpec = tween(750),
        label = "splashEntranceScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FindAHomeColors.PrimaryDarkBlue)
            .splashEntranceAlpha(entranceAlpha)
            .splashEntranceScale(entranceScale)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            SplashTitleCard()
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ImageCollage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    showLoader = isLoading && errorMessage == null
                ) {
                    AnimatedLoader(visible = true)
                }
            }

            SplashTaglineCard()
            Spacer(Modifier.height(28.dp))
        }

        if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp),
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
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

/** Top title — white card, subtle float + breath for a living, premium feel. */
@Composable
private fun SplashTitleCard() {
    val floatY = rememberSplashFloatY(layerIndex = 10, amplitudeDp = 4f)
    val breath = rememberSplashBreathScale(min = 0.99f, max = 1.01f, durationMs = 2600)

    Card(
        modifier = Modifier
            .offset(y = floatY)
            .shadow(10.dp, SplashCardShape)
            .splashEntranceScale(breath),
        shape = SplashCardShape,
        colors = CardDefaults.cardColors(containerColor = FindAHomeColors.CardSurface)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = FindAHomeColors.PrimaryText,
            modifier = Modifier.padding(horizontal = 36.dp, vertical = 18.dp)
        )
    }
}

/** Bottom tagline — delayed fade/slide for staged entrance. */
@Composable
private fun SplashTaglineCard() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(280)
        visible = true
    }

    FadeSlideIn(visible = visible, modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, SplashCardShape),
            shape = SplashCardShape,
            colors = CardDefaults.cardColors(containerColor = FindAHomeColors.CardSurface)
        ) {
            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = FindAHomeColors.PrimaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            )
        }
    }
}
