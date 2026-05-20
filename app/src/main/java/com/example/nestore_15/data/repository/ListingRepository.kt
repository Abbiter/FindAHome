package com.example.nestore_15.data.repository

import android.net.Uri
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.util.LocalListingImages
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ListingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun getAllListings(): Flow<List<Listing>> {
        return combine(
            listingsFlow(firestore.collection("listings")),
            propertiesAsListingsFlow()
        ) { legacy, fromProperties ->
            mergeListings(legacy, fromProperties)
        }
    }

    fun getFilteredListings(
        minPriceBwp: Double? = null,
        maxPriceBwp: Double? = null,
        location: String? = null
    ): Flow<List<Listing>> {
        var query: Query = firestore.collection("listings")
            .whereGreaterThanOrEqualTo("availabilityDate", todayDateString())

        if (minPriceBwp != null) {
            query = query.whereGreaterThanOrEqualTo("priceBwp", minPriceBwp)
        }

        if (maxPriceBwp != null) {
            query = query.whereLessThanOrEqualTo("priceBwp", maxPriceBwp)
        }

        val locationFilter = location?.trim()
        if (!locationFilter.isNullOrEmpty()) {
            query = query.whereEqualTo("location", locationFilter)
        }

        return combine(
            listingsFlow(query),
            propertiesAsListingsFlow()
        ) { legacy, propertyRows ->
            val filteredProperties = propertyRows.filter { listing ->
                passesClientFilters(
                    listing = listing,
                    minPriceBwp = minPriceBwp,
                    maxPriceBwp = maxPriceBwp,
                    location = locationFilter,
                    minAvailabilityDate = todayDateString()
                )
            }
            mergeListings(legacy, filteredProperties)
        }
    }

    private fun propertiesAsListingsFlow(): Flow<List<Listing>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection("properties")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents.orEmpty()
                    .mapNotNull { it.toPropertyOrNull()?.toListing() }
                trySend(list).isSuccess
            }
        awaitClose { registration.remove() }
    }

    private fun mergeListings(legacy: List<Listing>, fromProperties: List<Listing>): List<Listing> {
        val byId = LinkedHashMap<String, Listing>()
        legacy.forEach { byId[it.id] = it }
        fromProperties.forEach { byId[it.id] = it }
        return byId.values.sortedByDescending { it.availabilityDate }
    }

    private fun passesClientFilters(
        listing: Listing,
        minPriceBwp: Double?,
        maxPriceBwp: Double?,
        location: String?,
        minAvailabilityDate: String
    ): Boolean {
        if (listing.availabilityDate < minAvailabilityDate) return false
        if (minPriceBwp != null && listing.priceBwp < minPriceBwp) return false
        if (maxPriceBwp != null && listing.priceBwp > maxPriceBwp) return false
        if (!location.isNullOrBlank() && !listing.location.equals(location, ignoreCase = true)) {
            return false
        }
        return true
    }

    private fun listingsFlow(query: Query): Flow<List<Listing>> = callbackFlow {
        val registration: ListenerRegistration = query
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()).isSuccess
                    return@addSnapshotListener
                }

                val listings = snapshot?.documents.orEmpty().mapNotNull { document ->
                    document.toListingOrNull()
                }
                trySend(listings).isSuccess
            }

        awaitClose { registration.remove() }
    }

    suspend fun getListingById(id: String): Listing? {
        return runCatching {
            val snap = firestore.collection("listings").document(id).get().await()
            if (!snap.exists()) null else snap.toListingOrNull()
        }.getOrNull()
    }

    suspend fun reserveListing(id: String, currentUserId: String): String {
        val reservationRef = UUID.randomUUID().toString()
        val listingRef = firestore.collection("listings").document(id)

        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(listingRef)
            val alreadyReserved = snapshot.getBoolean("isReserved") ?: false
            if (alreadyReserved) {
                throw IllegalStateException("Listing already reserved")
            }

            transaction.update(
                listingRef,
                mapOf(
                    "isReserved" to true,
                    "reservedBy" to currentUserId,
                    "reservationRef" to reservationRef
                )
            )
            reservationRef
        }.await()
    }

    /**
     * Legacy listings collection — images stored as drawable keys (no Firebase Storage).
     * [imageUri] is ignored; kept for call-site compatibility.
     */
    suspend fun createListing(
        listing: Listing,
        imageUri: Uri? = null,
        imageDrawableKey: String = LocalListingImages.defaultPropertyImageKey
    ): String {
        val listingId = if (listing.id.isBlank()) {
            firestore.collection("listings").document().id
        } else {
            listing.id
        }

        val imageUrl = imageDrawableKey.ifBlank { LocalListingImages.defaultPropertyImageKey }

        val payload = hashMapOf(
            "title" to listing.title,
            "priceBwp" to listing.priceBwp,
            "location" to listing.location,
            "type" to listing.type,
            "amenities" to listing.amenities,
            "availabilityDate" to listing.availabilityDate,
            "depositAmount" to listing.depositAmount,
            "imageUrl" to imageUrl,
            "isReserved" to listing.isReserved,
            "ownerId" to listing.ownerId
        )
        firestore.collection("listings").document(listingId).set(payload).await()
        return listingId
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toListingOrNull(): Listing? {
        val title = getString("title") ?: return null
        val price = getDouble("priceBwp")
            ?: getLong("priceBwp")?.toDouble()
            ?: return null
        val location = getString("location") ?: return null
        val type = getString("type") ?: ""
        val amenities = (get("amenities") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        val availabilityDate = getString("availabilityDate") ?: ""
        val depositAmount = getDouble("depositAmount") ?: 0.0
        val imageUrl = getString("imageUrl") ?: ""
        val isReserved = getBoolean("isReserved") ?: false
        val ownerId = getString("ownerId") ?: ""

        return Listing(
            id = id,
            title = title,
            priceBwp = price,
            location = location,
            type = type,
            amenities = amenities,
            availabilityDate = availabilityDate,
            depositAmount = depositAmount,
            imageUrl = imageUrl,
            ownerId = ownerId,
            isReserved = isReserved,
            isPropertyListing = false
        )
    }

    private fun todayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}
