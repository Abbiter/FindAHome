package com.example.nestore_15.data.repository

import android.net.Uri
import com.example.nestore_15.data.model.Listing
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ListingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    fun getAllListings(): Flow<List<Listing>> {
        return listingsFlow(firestore.collection("listings"))
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

        return listingsFlow(query)
    }

    private fun listingsFlow(query: Query): Flow<List<Listing>> = callbackFlow {
        val registration: ListenerRegistration = query
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val listings = snapshot?.documents.orEmpty().mapNotNull { document ->
                    document.toListingOrNull()
                }
                trySend(listings).isSuccess
            }

        awaitClose { registration.remove() }
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

    suspend fun createListing(listing: Listing, imageUri: Uri): String {
        val listingId = if (listing.id.isBlank()) {
            firestore.collection("listings").document().id
        } else {
            listing.id
        }

        val imageRef = storage.reference.child("listing_images/$listingId")
        imageRef.putFile(imageUri).await()
        val imageUrl = imageRef.downloadUrl.await().toString()

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
        val price = getDouble("priceBwp") ?: return null
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
            isReserved = isReserved
        )
    }

    private fun todayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}
