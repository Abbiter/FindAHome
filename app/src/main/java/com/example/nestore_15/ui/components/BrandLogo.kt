package com.example.nestore_15.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.example.nestore_15.R

@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    height: Dp = Dp.Unspecified,
    width: Dp = Dp.Unspecified,
    fillMaxWidthFraction: Float? = null,
    contentDescription: String = "Find A Home"
) {
    var imageModifier = modifier
    if (fillMaxWidthFraction != null) {
        imageModifier = imageModifier
            .fillMaxWidth(fillMaxWidthFraction)
            .aspectRatio(1f)
    }
    if (height != Dp.Unspecified) {
        imageModifier = imageModifier.height(height)
    }
    if (width != Dp.Unspecified) {
        imageModifier = imageModifier.width(width)
    }
    Image(
        painter = painterResource(R.drawable.splash_logo),
        contentDescription = contentDescription,
        modifier = imageModifier,
        contentScale = ContentScale.Fit
    )
}
