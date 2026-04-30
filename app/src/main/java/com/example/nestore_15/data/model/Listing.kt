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
    val ownerId: String,
    val isReserved: Boolean = false
)
