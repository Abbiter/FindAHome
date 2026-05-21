package com.example.nestore_15.data.util

/**
 * Default property photos when gallery picks are not uploaded to Storage.
 * Stores remote housing URLs in Firestore — not splash-screen drawable keys.
 */
object LocalListingImages {

    @Deprecated("Legacy drawable key; migrated to remote housing URL at read time")
    const val KEY_INTERIOR = "listing_interior"

    @Deprecated("Legacy drawable key; migrated to remote housing URL at read time")
    const val KEY_MOVING = "listing_moving"

    @Deprecated("Legacy drawable key; migrated to remote housing URL at read time")
    const val KEY_LIFESTYLE = "listing_lifestyle"

    fun urlsForNewProperty(pickedImageCount: Int): List<String> =
        DefaultPropertyImageUrls.urlsForNewProperty(pickedImageCount)

    fun urlsForAdditionalImages(additionalCount: Int, startIndex: Int): List<String> =
        DefaultPropertyImageUrls.urlsForAdditionalImages(additionalCount, startIndex)

    fun urlForListingId(listingId: String): String =
        DefaultPropertyImageUrls.urlForListingId(listingId)
}
