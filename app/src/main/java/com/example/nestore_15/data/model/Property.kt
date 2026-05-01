package com.example.nestore_15.data.model

enum class PropertyStatus {
    AVAILABLE,
    PENDING,
    RENTED;

    companion object {
        fun fromFirestore(value: String?): PropertyStatus =
            runCatching { valueOf(value ?: "") }.getOrDefault(AVAILABLE)
    }
}

data class Property(
    val id: String,
    val ownerId: String,
    val title: String,
    val description: String,
    val location: String,
    val priceBwp: Double,
    val roomCount: Int,
    val availabilityStatus: PropertyStatus,
    /** yyyy-MM-dd for filters / display */
    val availabilityDate: String,
    val imageUrls: List<String>,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
