package com.example.nestore_15.data.repository

import com.example.nestore_15.data.model.ChatMessage
import com.example.nestore_15.data.model.ConversationSummary
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getOrCreateConversation(
        propertyId: String,
        propertyTitle: String,
        propertyImageUrl: String,
        studentId: String,
        providerId: String
    ): String {
        val participants = listOf(studentId, providerId).sorted()
        val conversationId = "${propertyId}_${participants[0]}_${participants[1]}"
        val ref = firestore.collection("conversations").document(conversationId)
        val existing = ref.get().await()
        if (existing.exists()) return conversationId

        val studentName = fetchUserDisplayName(studentId, fallback = "Student")
        val providerName = fetchUserDisplayName(providerId, fallback = "Provider")

        ref.set(
            mapOf(
                "participants" to participants,
                "studentId" to studentId,
                "providerId" to providerId,
                "studentName" to studentName,
                "providerName" to providerName,
                "propertyId" to propertyId,
                "propertyTitle" to propertyTitle,
                "propertyImageUrl" to propertyImageUrl,
                "lastMessage" to "",
                "lastUpdated" to FieldValue.serverTimestamp(),
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
        return conversationId
    }

    fun observeMessages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val senderId = doc.getString("senderId") ?: return@mapNotNull null
                    val message = doc.getString("message") ?: return@mapNotNull null
                    val ts = doc.getTimestamp("timestamp") ?: Timestamp.now()
                    ChatMessage(
                        id = doc.id,
                        senderId = senderId,
                        message = message,
                        timestamp = ts.toDate().time
                    )
                }
                trySend(messages).isSuccess
            }

        awaitClose { registration.remove() }
    }

    suspend fun sendMessage(conversationId: String, senderId: String, message: String) {
        val conversationRef = firestore.collection("conversations").document(conversationId)
        conversationRef
            .collection("messages")
            .add(
                mapOf(
                    "senderId" to senderId,
                    "message" to message,
                    "timestamp" to FieldValue.serverTimestamp()
                )
            )
            .await()
        conversationRef.update(
            mapOf(
                "lastMessage" to message,
                "lastUpdated" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    fun observeConversations(currentUserId: String): Flow<List<ConversationSummary>> = callbackFlow {
        val registration = firestore.collection("conversations")
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastUpdated")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val rows = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    ConversationSummary(
                        id = doc.id,
                        propertyId = doc.getString("propertyId").orEmpty(),
                        propertyTitle = doc.getString("propertyTitle").orEmpty(),
                        propertyImageUrl = doc.getString("propertyImageUrl").orEmpty(),
                        participants = doc.get("participants") as? List<String> ?: emptyList(),
                        studentId = doc.getString("studentId").orEmpty(),
                        providerId = doc.getString("providerId").orEmpty(),
                        studentName = doc.getString("studentName").orEmpty(),
                        providerName = doc.getString("providerName").orEmpty(),
                        lastMessage = doc.getString("lastMessage").orEmpty(),
                        lastUpdated = (doc.getTimestamp("lastUpdated") ?: Timestamp.now()).toDate().time
                    )
                }.sortedByDescending { it.lastUpdated }
                trySend(rows).isSuccess
            }
        awaitClose { registration.remove() }
    }

    suspend fun getConversation(conversationId: String): ConversationSummary? {
        val doc = firestore.collection("conversations").document(conversationId).get().await()
        if (!doc.exists()) return null
        return ConversationSummary(
            id = doc.id,
            propertyId = doc.getString("propertyId").orEmpty(),
            propertyTitle = doc.getString("propertyTitle").orEmpty(),
            propertyImageUrl = doc.getString("propertyImageUrl").orEmpty(),
            participants = doc.get("participants") as? List<String> ?: emptyList(),
            studentId = doc.getString("studentId").orEmpty(),
            providerId = doc.getString("providerId").orEmpty(),
            studentName = doc.getString("studentName").orEmpty(),
            providerName = doc.getString("providerName").orEmpty(),
            lastMessage = doc.getString("lastMessage").orEmpty(),
            lastUpdated = (doc.getTimestamp("lastUpdated") ?: Timestamp.now()).toDate().time
        )
    }

    private suspend fun fetchUserDisplayName(userId: String, fallback: String): String {
        val doc = firestore.collection("users").document(userId).get().await()
        val fullName = doc.getString("fullName").orEmpty().trim()
        if (fullName.isNotEmpty()) return fullName
        val email = doc.getString("email").orEmpty()
        return email.substringBefore("@").takeIf { it.isNotBlank() } ?: fallback
    }
}
