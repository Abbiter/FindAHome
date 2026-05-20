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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nestore_15.R
import com.example.nestore_15.ui.components.BrandLogo
import com.example.nestore_15.ui.components.PrimaryOrangeButton
import com.example.nestore_15.ui.theme.FindAHomeColors

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
        animationSpec = tween(600),
        label = "splashAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FindAHomeColors.PrimaryDarkBlue)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 36.dp, vertical = 40.dp)
                .alpha(entranceAlpha)
        ) {
            BrandLogo(
                fillMaxWidthFraction = 0.62f,
                modifier = Modifier
                    .graphicsLayer { clip = false }
                    .padding(bottom = 8.dp)
            )
            if (isLoading && errorMessage == null) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 36.dp)
                        .size(44.dp),
                    color = FindAHomeColors.OrangeAccent,
                    strokeWidth = 4.dp
                )
            }
            Spacer(Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = FindAHomeColors.TextOnPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(3.dp)
                .navigationBarsPadding()
                .background(FindAHomeColors.OrangeAccent)
        )

        if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp, vertical = 48.dp)
                    .navigationBarsPadding(),
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
