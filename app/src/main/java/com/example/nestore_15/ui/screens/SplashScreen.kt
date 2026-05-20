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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nestore_15.R
import com.example.nestore_15.ui.components.BrandLogo
import com.example.nestore_15.ui.components.PrimaryOrangeButton
import com.example.nestore_15.ui.components.PulsingLoader
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
        animationSpec = tween(750),
        label = "splashAlpha"
    )
    val entranceScale by animateFloatAsState(
        targetValue = if (entranceStarted) 1f else 0.92f,
        animationSpec = tween(750),
        label = "splashScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FindAHomeColors.PrimaryDarkBlue)
            .statusBarsPadding()
            .alpha(entranceAlpha)
            .scale(entranceScale),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            BrandLogo(
                modifier = Modifier.size(220.dp),
                height = 220.dp,
                width = 220.dp
            )
            if (isLoading && errorMessage == null) {
                PulsingLoader(modifier = Modifier.padding(top = 28.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = FindAHomeColors.TextOnPrimary,
                textAlign = TextAlign.Center
            )
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
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}
