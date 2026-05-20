package com.example.nestore_15.data.repository

import android.net.Uri
import com.example.nestore_15.data.model.Listing
import com.example.nestore_15.data.model.Property
import com.example.nestore_15.data.model.PropertyStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.example.nestore_15.data.util.LocalListingImages
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

internal fun DocumentSnapshot.toPropertyOrNull(): Property? {
    val ownerId = getString("ownerId") ?: return null
    val title = getString("title") ?: return null
    val description = getString("description").orEmpty()
    val location = getString("location") ?: return null
    val price = getDouble("priceBwp")
        ?: getLong("priceBwp")?.toDouble()
        ?: return null
    val rooms = getLong("roomCount")?.toInt()
        ?: getDouble("roomCount")?.toInt()
        ?: 0
    val status = PropertyStatus.fromFirestore(getString("availabilityStatus"))
    val availabilityDate = getString("availabilityDate").orEmpty()
    val urls = (get("imageUrls") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    val createdAt = getLong("createdAt")
    val updatedAt = getLong("updatedAt")
    return Property(
        id = id,
        ownerId = ownerId,
        title = title,
        description = description,
        location = location,
        priceBwp = price,
        roomCount = rooms,
        availabilityStatus = status,
        availabilityDate = availabilityDate,
        imageUrls = urls,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Property.toListing(): Listing {
    val isRented = availabilityStatus == PropertyStatus.RENTED
    val dateForFilter = when (availabilityStatus) {
        PropertyStatus.AVAILABLE -> availabilityDate.ifBlank { todayString() }
        PropertyStatus.PENDING -> availabilityDate.ifBlank {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() + 86400000L * 30))
        }
        PropertyStatus.RENTED -> "2000-01-01"
    }
    return Listing(
        id = id,
        title = title,
        priceBwp = priceBwp,
        location = location,
        type = "Property",
        amenities = emptyList(),
        availabilityDate = dateForFilter,
        depositAmount = 0.0,
        imageUrl = imageUrls.firstOrNull().orEmpty(),
        ownerId = ownerId,
        isReserved = isRented,
        isPropertyListing = true
    )
}

private fun todayString(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

class PropertyRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun observePropertiesByOwner(ownerId: String): Flow<List<Property>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection("properties")
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()).isSuccess
                    return@addSnapshotListener
                }
                val list = snapshot?.documents.orEmpty().mapNotNull { it.toPropertyOrNull() }
                trySend(list).isSuccess
            }
        awaitClose { registration.remove() }
    }

    fun observeAllProperties(): Flow<List<Property>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection("properties")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList()).isSuccess
                    return@addSnapshotListener
                }
                val list = snapshot?.documents.orEmpty().mapNotNull { it.toPropertyOrNull() }
                trySend(list).isSuccess
            }
        awaitClose { registration.remove() }
    }

    suspend fun getProperty(propertyId: String): Property? {
        val snap = firestore.collection("properties").document(propertyId).get().await()
        return if (snap.exists()) snap.toPropertyOrNull() else null
    }

    suspend fun createProperty(
        ownerId: String,
        title: String,
        description: String,
        location: String,
        priceBwp: Double,
        roomCount: Int,
        availabilityStatus: PropertyStatus,
        availabilityDate: String,
        imageUris: List<Uri>
    ): String {
        val docId = firestore.collection("properties").document().id
        val imageUrls = LocalListingImages.keysForNewProperty(imageUris.size)
        val now = System.currentTimeMillis()
        val payload = hashMapOf(
            "ownerId" to ownerId,
            "title" to title,
            "description" to description,
            "location" to location,
            "priceBwp" to priceBwp,
            "roomCount" to roomCount,
            "availabilityStatus" to availabilityStatus.name,
            "availabilityDate" to availabilityDate.ifBlank { todayString() },
            "imageUrls" to imageUrls,
            "createdAt" to now,
            "updatedAt" to now
        )
        firestore.collection("properties").document(docId).set(payload).await()
        return docId
    }

    suspend fun updateProperty(
        propertyId: String,
        ownerId: String,
        title: String,
        description: String,
        location: String,
        priceBwp: Double,
        roomCount: Int,
        availabilityStatus: PropertyStatus,
        availabilityDate: String,
        existingImageUrls: List<String>,
        newImageUris: List<Uri>
    ) {
        val newUrls = LocalListingImages.keysForAdditionalImages(
            additionalCount = newImageUris.size,
            startIndex = existingImageUrls.size
        )
        val mergedUrls = existingImageUrls + newUrls
        val now = System.currentTimeMillis()
        val payload = hashMapOf(
            "ownerId" to ownerId,
            "title" to title,
            "description" to description,
            "location" to location,
            "priceBwp" to priceBwp,
            "roomCount" to roomCount,
            "availabilityStatus" to availabilityStatus.name,
            "availabilityDate" to availabilityDate.ifBlank { todayString() },
            "imageUrls" to mergedUrls,
            "updatedAt" to now
        )
        @Suppress("UNCHECKED_CAST")
        firestore.collection("properties").document(propertyId).update(payload as Map<String, Any>).await()
    }

    suspend fun updateAvailabilityStatus(propertyId: String, status: PropertyStatus) {
        firestore.collection("properties").document(propertyId).update(
            mapOf(
                "availabilityStatus" to status.name,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun reservePropertyAsRented(propertyId: String, reservedBy: String): String {
        val reservationRef = UUID.randomUUID().toString()
        val docRef = firestore.collection("properties").document(propertyId)
        return firestore.runTransaction { tx ->
            val snap = tx.get(docRef)
            if (!snap.exists()) {
                throw IllegalStateException("Property not found")
            }
            val status = PropertyStatus.fromFirestore(snap.getString("availabilityStatus"))
            if (status == PropertyStatus.RENTED) {
                throw IllegalStateException("Listing already reserved")
            }
            tx.update(
                docRef,
                mapOf(
                    "availabilityStatus" to PropertyStatus.RENTED.name,
                    "reservedBy" to reservedBy,
                    "reservationRef" to reservationRef,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            reservationRef
        }.await()
    }

    suspend fun deleteProperty(propertyId: String) {
        val inquiries = firestore.collection("inquiries")
            .whereEqualTo("propertyId", propertyId)
            .get()
            .await()
        inquiries.documents.forEach { it.reference.delete().await() }
        firestore.collection("properties").document(propertyId).delete().await()
    }

}
