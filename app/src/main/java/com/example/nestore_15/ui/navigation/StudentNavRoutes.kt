package com.example.nestore_15.ui.navigation

object StudentNavRoutes {
    const val HOME = "home"
    const val LISTING_DETAILS = "listing/{listingId}"
    const val PAYMENT = "payment/{listingId}"

    fun listingDetails(listingId: String) = "listing/$listingId"
    fun payment(listingId: String) = "payment/$listingId"
}
