package com.example.nestore_15.data.util

/**
 * Default housing photos (rooms / apartments / houses only).
 * Used when Firestore has no upload URL or still has legacy drawable keys.
 */
object DefaultPropertyImageUrls {

    private val housingUrls: List<String> = listOf(
        "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800&q=80",
        "https://images.unsplash.com/photo-1522708323590-d24dbb521c0c?w=800&q=80",
        "https://images.unsplash.com/photo-1560448204-e4f9c3e1e631?w=800&q=80",
        "https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=80",
        "https://images.unsplash.com/photo-1560185127-872d1bc887ff?w=800&q=80",
        "https://images.unsplash.com/photo-1560185007-cde436f6a4d8?w=800&q=80",
        "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=800&q=80",
        "https://images.unsplash.com/photo-1600566753190-17f0baa424a8?w=800&q=80",
        "https://images.pexels.com/photos/259588/pexels-photo-259588.jpeg?auto=compress&cs=tinysrgb&w=800",
        "https://images.pexels.com/photos/1396122/pexels-photo-1396122.jpeg?auto=compress&cs=tinysrgb&w=800",
        "https://images.pexels.com/photos/164338/pexels-photo-164338.jpeg?auto=compress&cs=tinysrgb&w=800",
        "https://images.pexels.com/photos/323705/pexels-photo-323705.jpeg?auto=compress&cs=tinysrgb&w=800",
        "https://images.pexels.com/photos/276724/pexels-photo-276724.jpeg?auto=compress&cs=tinysrgb&w=800",
        "https://images.pexels.com/photos/439391/pexels-photo-439391.jpeg?auto=compress&cs=tinysrgb&w=800"
    )

    /** Legacy Firestore values that pointed at splash-screen people photos. */
    val legacyDrawableKeys: Set<String> = setOf(
        LocalListingImages.KEY_INTERIOR,
        LocalListingImages.KEY_MOVING,
        LocalListingImages.KEY_LIFESTYLE,
        "splash_collage_interior",
        "splash_collage_moving",
        "splash_collage_lifestyle"
    )

    fun isLegacyDrawableKey(ref: String?): Boolean =
        ref?.trim()?.let { legacyDrawableKeys.contains(it) } == true

    fun urlForLegacyKey(key: String): String {
        val index = key.hashCode().and(Int.MAX_VALUE) % housingUrls.size
        return housingUrls[index]
    }

    fun urlForListingId(listingId: String): String {
        if (listingId.isBlank()) return housingUrls.first()
        val index = listingId.hashCode().and(Int.MAX_VALUE) % housingUrls.size
        return housingUrls[index]
    }

    fun urlsForNewProperty(pickedImageCount: Int): List<String> {
        val count = pickedImageCount.coerceIn(1, 3)
        val start = (System.currentTimeMillis() % housingUrls.size).toInt()
        return List(count) { offset ->
            housingUrls[(start + offset) % housingUrls.size]
        }
    }

    fun urlsForAdditionalImages(additionalCount: Int, startIndex: Int): List<String> {
        if (additionalCount <= 0) return emptyList()
        return List(additionalCount) { offset ->
            housingUrls[(startIndex + offset) % housingUrls.size]
        }
    }
}
