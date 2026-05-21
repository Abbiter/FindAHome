package com.example.nestore_15.data.util

import android.content.Context
import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.nestore_15.R
import com.example.nestore_15.data.model.Listing

/**
 * Resolves listing/property image references from Firestore.
 * Remote housing URLs only — splash collage / people photos are never used here.
 */
object ListingImageResolver {

    private const val TAG = "ListingImageResolver"

    @DrawableRes
    val fallbackDrawable: Int = R.drawable.ic_listing_placeholder

    sealed class ResolvedImage {
        data class Remote(val url: String) : ResolvedImage()
        data class Local(@DrawableRes val resId: Int) : ResolvedImage()
    }

    fun isRemote(imageRef: String?): Boolean {
        val ref = imageRef?.trim().orEmpty()
        return ref.startsWith("http://", ignoreCase = true) ||
            ref.startsWith("https://", ignoreCase = true)
    }

    /**
     * Resolves a single image reference string to remote URL or neutral property placeholder.
     */
    fun resolve(context: Context, imageRef: String?): ResolvedImage {
        val ref = imageRef?.trim().orEmpty()
        if (ref.isEmpty()) {
            return ResolvedImage.Local(fallbackDrawable)
        }
        if (isRemote(ref)) {
            return ResolvedImage.Remote(ref)
        }
        if (DefaultPropertyImageUrls.isLegacyDrawableKey(ref)) {
            return ResolvedImage.Remote(DefaultPropertyImageUrls.urlForLegacyKey(ref))
        }
        val resId = resolveDrawableRes(context, ref)
        return ResolvedImage.Local(resId)
    }

    fun primaryFromList(imageUrls: List<String>?): String? =
        imageUrls?.firstOrNull { it.isNotBlank() }

    fun displayRefForListing(listing: Listing): String {
        primaryFromList(listing.imageUrls)?.let { ref ->
            if (DefaultPropertyImageUrls.isLegacyDrawableKey(ref)) {
                return DefaultPropertyImageUrls.urlForLegacyKey(ref)
            }
            return ref
        }
        val single = listing.imageUrl.trim()
        if (single.isNotEmpty()) {
            if (DefaultPropertyImageUrls.isLegacyDrawableKey(single)) {
                return DefaultPropertyImageUrls.urlForLegacyKey(single)
            }
            return single
        }
        return LocalListingImages.urlForListingId(listing.id)
    }

    @DrawableRes
    fun catalogPlaceholderForId(@Suppress("UNUSED_PARAMETER") listingId: String): Int =
        fallbackDrawable

    fun glideModel(context: Context, imageRef: String?): Any {
        return when (val resolved = resolve(context, imageRef)) {
            is ResolvedImage.Remote -> resolved.url
            is ResolvedImage.Local -> resolved.resId
        }
    }

    @DrawableRes
    fun resolveDrawableRes(context: Context, drawableKey: String): Int {
        val key = drawableKey.trim()
        val fromName = context.resources.getIdentifier(key, "drawable", context.packageName)
        if (fromName != 0 && !isSplashCollageDrawable(fromName)) {
            return fromName
        }
        logMissing(context, key, fallbackDrawable)
        return fallbackDrawable
    }

    private fun isSplashCollageDrawable(@DrawableRes resId: Int): Boolean =
        resId == R.drawable.splash_collage_interior ||
            resId == R.drawable.splash_collage_moving ||
            resId == R.drawable.splash_collage_lifestyle

    private fun logMissing(context: Context, key: String, @DrawableRes fallback: Int) {
        if (key.isEmpty()) return
        Log.w(
            TAG,
            "Unknown image key \"$key\" in ${context.packageName}; using property placeholder ($fallback)"
        )
    }
}

fun ImageView.loadListingImage(
    imageRef: String?,
    placeholder: Int = ListingImageResolver.fallbackDrawable,
    error: Int = ListingImageResolver.fallbackDrawable
) {
    Glide.with(context)
        .load(ListingImageResolver.glideModel(context, imageRef))
        .placeholder(placeholder)
        .error(error)
        .transition(DrawableTransitionOptions.withCrossFade())
        .centerCrop()
        .into(this)
}
