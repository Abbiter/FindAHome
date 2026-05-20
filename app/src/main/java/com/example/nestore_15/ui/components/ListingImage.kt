package com.example.nestore_15.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.nestore_15.data.util.ListingImageResolver

/**
 * Compose entry point for listing/property images.
 * Delegates to [ListingImageResolver] for URL vs drawable resolution.
 */
@Composable
fun ListingImage(
    imageRef: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val resolved = remember(imageRef) { ListingImageResolver.resolve(context, imageRef) }
    when (resolved) {
        is ListingImageResolver.ResolvedImage.Remote -> {
            AsyncImage(
                model = resolved.url,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        is ListingImageResolver.ResolvedImage.Local -> {
            Image(
                painter = painterResource(resolved.resId),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
    }
}
