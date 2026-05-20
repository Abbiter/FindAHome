package com.example.nestore_15.data.model

data class Listing(
    val id: String,
    val title: String,
    val priceBwp: Double,
    val location: String,
    val type: String,
    val amenities: List<String>,
    val availabilityDate: String,
    val depositAmount: Double,
    val imageUrl: String,
    /** All image refs when sourced from a property document (`imageUrls` in Firestore). */
    val imageUrls: List<String> = emptyList(),
    val ownerId: String,
    val isReserved: Boolean = false,
    val reservedBy: String = "",
    val reservationRef: String = "",
    /** True when this row comes from the Firestore `properties` collection (not legacy `listings`). */
    val isPropertyListing: Boolean = false
) {
    fun isReservedBy(userId: String): Boolean =
        userId.isNotBlank() && reservedBy == userId
}
