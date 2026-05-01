package com.example.nestore_15.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UserRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    suspend fun uploadProfilePhoto(userId: String, uri: Uri): String {
        val ref = storage.reference.child("user_profiles/$userId/photo_${UUID.randomUUID()}")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadVerificationDocument(userId: String, uri: Uri): String {
        val ref = storage.reference.child("user_verification/$userId/doc_${UUID.randomUUID()}")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadOwnershipProof(userId: String, uri: Uri): String {
        val ref = storage.reference.child("user_ownership/$userId/proof_${UUID.randomUUID()}")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
