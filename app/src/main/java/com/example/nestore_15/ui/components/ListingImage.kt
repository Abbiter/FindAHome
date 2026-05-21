package com.example.nestore_15.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
    contentScale: ContentScale = ContentScale.Crop,
    listingId: String = ""
) {
    val context = LocalContext.current
    val resolved = remember(imageRef) { ListingImageResolver.resolve(context, imageRef) }
    val placeholderRes = remember(listingId) {
        ListingImageResolver.catalogPlaceholderForId(listingId)
    }
    val placeholderPainter = painterResource(placeholderRes)
    when (resolved) {
        is ListingImageResolver.ResolvedImage.Remote -> {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resolved.url)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale,
                placeholder = placeholderPainter,
                error = placeholderPainter
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
