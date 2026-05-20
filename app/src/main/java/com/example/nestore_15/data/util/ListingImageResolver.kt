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
 *
 * Supports:
 * - Remote URLs (`http` / `https`) for legacy Firebase Storage links
 * - Local drawable keys (e.g. `listing_interior`) for assignment-friendly assets
 *
 * Firestore fields stay as strings; no schema change required for Phase 1.
 */
object ListingImageResolver {

    private const val TAG = "ListingImageResolver"

    /**
     * Maps seed/Firestore keys to existing drawable resources when names differ.
     * Add entries here when seed data uses shorthand keys.
     */
    private val drawableAliases: Map<String, Int> = mapOf(
        "listing_interior" to R.drawable.splash_collage_interior,
        "listing_moving" to R.drawable.splash_collage_moving,
        "listing_lifestyle" to R.drawable.splash_collage_lifestyle,
        "splash_collage_interior" to R.drawable.splash_collage_interior,
        "splash_collage_moving" to R.drawable.splash_collage_moving,
        "splash_collage_lifestyle" to R.drawable.splash_collage_lifestyle
    )

    @DrawableRes
    val fallbackDrawable: Int = R.drawable.profile_placeholder

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
     * Resolves a single image reference string to remote URL or local drawable.
     */
    fun resolve(context: Context, imageRef: String?): ResolvedImage {
        val ref = imageRef?.trim().orEmpty()
        if (ref.isEmpty()) {
            logMissing(context, ref, fallbackDrawable)
            return ResolvedImage.Local(fallbackDrawable)
        }
        if (isRemote(ref)) {
            return ResolvedImage.Remote(ref)
        }
        val resId = resolveDrawableRes(context, ref)
        return ResolvedImage.Local(resId)
    }

    /** First non-blank entry from a property image list. */
    fun primaryFromList(imageUrls: List<String>?): String? =
        imageUrls?.firstOrNull { it.isNotBlank() }

    /** Best ref for cards: remote/local from list, then single field, then catalog fallback. */
    fun displayRefForListing(listing: Listing): String {
        primaryFromList(listing.imageUrls)?.let { return it }
        val single = listing.imageUrl.trim()
        if (single.isNotEmpty()) return single
        return LocalListingImages.keyForListingId(listing.id)
    }

    @DrawableRes
    fun catalogPlaceholderForId(listingId: String): Int {
        val key = LocalListingImages.keyForListingId(listingId)
        return drawableAliases[key] ?: fallbackDrawable
    }

    /**
     * Model object for Glide: [String] URL or [@DrawableRes] Int.
     */
    fun glideModel(context: Context, imageRef: String?): Any {
        return when (val resolved = resolve(context, imageRef)) {
            is ResolvedImage.Remote -> resolved.url
            is ResolvedImage.Local -> resolved.resId
        }
    }

    @DrawableRes
    fun resolveDrawableRes(context: Context, drawableKey: String): Int {
        val key = drawableKey.trim()
        drawableAliases[key]?.let { return it }

        val fromName = context.resources.getIdentifier(key, "drawable", context.packageName)
        if (fromName != 0) {
            return fromName
        }

        logMissing(context, key, fallbackDrawable)
        return fallbackDrawable
    }

    private fun logMissing(context: Context, key: String, @DrawableRes fallback: Int) {
        if (key.isEmpty()) return
        Log.w(
            TAG,
            "Drawable not found for key \"$key\" in ${context.packageName}; using fallback ($fallback)"
        )
    }
}

/**
 * Loads a listing/property image into an [ImageView] (Glide).
 * Use in XML-based screens and RecyclerView adapters.
 */
fun ImageView.loadListingImage(
    imageRef: String?,
    placeholder: Int = R.drawable.profile_placeholder,
    error: Int = R.drawable.profile_placeholder
) {
    Glide.with(context)
        .load(ListingImageResolver.glideModel(context, imageRef))
        .placeholder(placeholder)
        .error(error)
        .transition(DrawableTransitionOptions.withCrossFade())
        .centerCrop()
        .into(this)
}
