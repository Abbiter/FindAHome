package com.example.nestore_15.ui.components

import androidx.compose.foundation.Image
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
    contentDescription: String = "Find A Home"
) {
    Image(
        painter = painterResource(R.drawable.branding_logo),
        contentDescription = contentDescription,
        modifier = modifier.then(
            when {
                height != Dp.Unspecified -> Modifier.height(height)
                else -> Modifier
            }
        ).then(
            when {
                width != Dp.Unspecified -> Modifier.width(width)
                else -> Modifier
            }
        ),
        contentScale = ContentScale.Fit
    )
}
