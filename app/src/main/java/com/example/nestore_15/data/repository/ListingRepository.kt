package com.example.nestore_15.data.repository

import android.net.Uri
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.util.ListingImageResolver
import com.example.nestore_15.data.util.LocalListingImages
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
            mergeListings(legacy, fromProperties, propertiesPrimary = true)
        }
    }

    /** Listings open for new reservations (excludes rented / reserved). */
    fun getBrowsableListings(): Flow<List<Listing>> =
        getAllListings().map { listings -> listings.filter { !it.isReserved } }

    fun observeReservedByUser(userId: String): Flow<List<Listing>> = combine(
        reservedLegacyListingsFlow(userId),
        reservedPropertiesAsListingsFlow(userId)
    ) { legacy, fromProperties ->
        mergeListings(legacy, fromProperties)
    }

    private fun reservedPropertiesAsListingsFlow(userId: String): Flow<List<Listing>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection("properties")
            .whereEqualTo("reservedBy", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()).isSuccess
                    return@addSnapshotListener
                }
                val list = snapshot?.documents.orEmpty()
                    .mapNotNull { it.toPropertyOrNull()?.toListing() }
                trySend(list).isSuccess
            }
        awaitClose { registration.remove() }
    }

    private fun reservedLegacyListingsFlow(userId: String): Flow<List<Listing>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection("listings")
            .whereEqualTo("reservedBy", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()).isSuccess
                    return@addSnapshotListener
                }
                val listings = snapshot?.documents.orEmpty().mapNotNull { it.toListingOrNull() }
                trySend(listings).isSuccess
            }
        awaitClose { registration.remove() }
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
            mergeListings(legacy, filteredProperties, propertiesPrimary = true)
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

    private fun mergeListings(
        legacy: List<Listing>,
        fromProperties: List<Listing>,
        propertiesPrimary: Boolean = false
    ): List<Listing> {
        if (propertiesPrimary && fromProperties.isNotEmpty()) {
            return fromProperties.sortedByDescending { it.availabilityDate }
        }
        val byId = LinkedHashMap<String, Listing>()
        legacy.forEach { byId[it.id] = it }
        fromProperties.forEach { existing ->
            val prior = byId[existing.id]
            byId[existing.id] = if (prior == null) {
                existing
            } else {
                pickRicherListing(prior, existing)
            }
        }
        return byId.values.sortedByDescending { it.availabilityDate }
    }

    private fun pickRicherListing(a: Listing, b: Listing): Listing =
        if (imageRichness(b) >= imageRichness(a)) b else a

    private fun imageRichness(listing: Listing): Int {
        val refs = if (listing.imageUrls.isNotEmpty()) listing.imageUrls else listOf(listing.imageUrl)
        val remoteCount = refs.count { ListingImageResolver.isRemote(it) }
        val localCount = refs.count { it.isNotBlank() && !ListingImageResolver.isRemote(it) }
        return remoteCount * 3 + localCount + if (listing.isPropertyListing) 1 else 0
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
        if (!location.isNullOrBlank()) {
            val needle = location.trim()
            if (!listing.location.contains(needle, ignoreCase = true)) return false
        }
        return true
    }

    fun applyBrowseFilters(
        listings: List<Listing>,
        minPriceBwp: Double?,
        maxPriceBwp: Double?,
        locationQuery: String?
    ): List<Listing> {
        val locationFilter = locationQuery?.trim().orEmpty()
        return listings.filter { listing ->
            if (listing.isReserved) return@filter false
            if (listing.reservedBy.isNotBlank()) return@filter false
            if (minPriceBwp != null && listing.priceBwp < minPriceBwp) return@filter false
            if (maxPriceBwp != null && listing.priceBwp > maxPriceBwp) return@filter false
            if (locationFilter.isNotEmpty() &&
                !listing.location.contains(locationFilter, ignoreCase = true)
            ) {
                return@filter false
            }
            true
        }
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
        imageUrl: String = LocalListingImages.urlForListingId("legacy")
    ): String {
        val listingId = if (listing.id.isBlank()) {
            firestore.collection("listings").document().id
        } else {
            listing.id
        }

        val resolvedImageUrl = imageUrl.ifBlank { LocalListingImages.urlForListingId("legacy") }

        val payload = hashMapOf(
            "title" to listing.title,
            "priceBwp" to listing.priceBwp,
            "location" to listing.location,
            "type" to listing.type,
            "amenities" to listing.amenities,
            "availabilityDate" to listing.availabilityDate,
            "depositAmount" to listing.depositAmount,
            "imageUrl" to resolvedImageUrl,
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
        val reservedBy = getString("reservedBy").orEmpty()
        val reservationRef = getString("reservationRef").orEmpty()

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
            reservedBy = reservedBy,
            reservationRef = reservationRef,
            isPropertyListing = false
        )
    }

    private fun todayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}
