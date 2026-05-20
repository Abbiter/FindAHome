package com.example.nestore_15.ui.splash

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nestore_15.R
import com.example.nestore_15.ui.theme.FindAHomeColors

/**
 * Layered property/lifestyle collage using the provided brand photography.
 *
 * Layout (Z-order back → front):
 * 1. Interior bedroom — hero, center-back
 * 2. Moving day — wide, bottom-left overlap
 * 3. Student lifestyle — top-right accent
 *
 * Responsive: sizes scale from [BoxWithConstraints] max width (reference 360dp wide phone).
 */
@Composable
fun ImageCollage(
    modifier: Modifier = Modifier,
    showLoader: Boolean = false,
    loaderContent: @Composable () -> Unit = {}
) {
    BoxWithConstraints(modifier = modifier) {
        val scale = (maxWidth / 360.dp).coerceIn(0.85f, 1.25f)

        val heroW = (200 * scale).dp
        val heroH = (240 * scale).dp
        val lifestyleW = (128 * scale).dp
        val lifestyleH = (158 * scale).dp
        val movingW = (210 * scale).dp
        val movingH = (118 * scale).dp

        Box(Modifier.fillMaxSize()) {
            // Layer 0 — interior (center, slightly elevated)
            CollagePhoto(
                resId = R.drawable.splash_collage_interior,
                width = heroW,
                height = heroH,
                layerIndex = 0,
                cornerRadius = 20.dp,
                elevation = 10.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-12 * scale).dp)
            )

            // Layer 1 — moving (bottom-start, overlaps hero)
            CollagePhoto(
                resId = R.drawable.splash_collage_moving,
                width = movingW,
                height = movingH,
                layerIndex = 1,
                cornerRadius = 16.dp,
                elevation = 12.dp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (4 * scale).dp, y = (8 * scale).dp)
            )

            // Layer 2 — lifestyle student (top-end)
            CollagePhoto(
                resId = R.drawable.splash_collage_lifestyle,
                width = lifestyleW,
                height = lifestyleH,
                layerIndex = 2,
                cornerRadius = 18.dp,
                elevation = 14.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-6 * scale).dp, y = (18 * scale).dp)
            )

            // Loader sits in the visual focal point between layers
            if (showLoader) {
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .offset(y = (36 * scale).dp)
                ) {
                    loaderContent()
                }
            }
        }
    }
}

@Composable
private fun CollagePhoto(
    resId: Int,
    width: Dp,
    height: Dp,
    layerIndex: Int,
    cornerRadius: Dp,
    elevation: Dp,
    modifier: Modifier = Modifier
) {
    val floatY = rememberSplashFloatY(layerIndex)
    val floatX = rememberSplashFloatX(layerIndex, amplitudeDp = if (layerIndex == 1) 6f else 4f)
    val shape = RoundedCornerShape(cornerRadius)

    Card(
        modifier = modifier
            .offset(x = floatX, y = floatY)
            .width(width)
            .height(height)
            .shadow(elevation, shape),
        shape = shape,
        border = BorderStroke(2.5.dp, FindAHomeColors.ImageBorder),
        colors = CardDefaults.cardColors(containerColor = FindAHomeColors.CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(shape),
            contentScale = ContentScale.Crop
        )
    }
}
