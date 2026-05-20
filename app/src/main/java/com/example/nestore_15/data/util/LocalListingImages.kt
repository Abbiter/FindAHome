package com.example.nestore_15.data.util

/**
 * Drawable keys stored in Firestore instead of Firebase Storage URLs.
 * Must match [ListingImageResolver] aliases or drawable resource names.
 */
object LocalListingImages {

    const val KEY_INTERIOR = "listing_interior"
    const val KEY_MOVING = "listing_moving"
    const val KEY_LIFESTYLE = "listing_lifestyle"

    val propertyImageKeys: List<String> = listOf(
        KEY_INTERIOR,
        KEY_MOVING,
        KEY_LIFESTYLE
    )

    val defaultPropertyImageKey: String = KEY_INTERIOR

    /**
     * Assigns drawable keys for a property save. Picked gallery URIs are not uploaded;
     * each selection maps to the next catalog key (assignment-friendly).
     */
    fun keysForNewProperty(pickedImageCount: Int): List<String> {
        if (pickedImageCount <= 0) {
            return listOf(defaultPropertyImageKey)
        }
        return List(pickedImageCount) { index ->
            propertyImageKeys[index % propertyImageKeys.size]
        }
    }

    /** Additional keys when editing and user picks more photos. */
    fun keysForAdditionalImages(additionalCount: Int, startIndex: Int): List<String> {
        if (additionalCount <= 0) return emptyList()
        return List(additionalCount) { offset ->
            propertyImageKeys[(startIndex + offset) % propertyImageKeys.size]
        }
    }
}
