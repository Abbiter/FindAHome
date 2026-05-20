package com.example.nestore_15.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nestore_15.ui.animation.rememberPulseScale
import com.example.nestore_15.ui.theme.FindAHomeColors

@Composable
fun PulsingLoader(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 48.dp) {
    val scale = rememberPulseScale()
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(size * scale),
            color = FindAHomeColors.OrangeAccent,
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun FullScreenLoading(message: String = "Loading…") {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PulsingLoader()
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = FindAHomeColors.TextSecondary,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun OverlayLoading(visible: Boolean) {
    if (visible) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PulsingLoader()
        }
    }
}
