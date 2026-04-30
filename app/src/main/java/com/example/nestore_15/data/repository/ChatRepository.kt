package com.example.nestore_15.data.repository

import com.example.nestore_15.data.model.ChatMessage
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

    suspend fun getOrCreateChat(
        listingId: String,
        currentUserId: String,
        ownerId: String
    ): String {
        val participants = listOf(currentUserId, ownerId).sorted()
        val chatId = "${listingId}_${participants[0]}_${participants[1]}"
        val chatRef = firestore.collection("chats").document(chatId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(chatRef)
            if (!snapshot.exists()) {
                transaction.set(
                    chatRef,
                    mapOf(
                        "chatId" to chatId,
                        "participants" to participants,
                        "listingId" to listingId
                    )
                )
            }
        }.await()

        return chatId
    }

    fun observeMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection("chats")
            .document(chatId)
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

    suspend fun sendMessage(chatId: String, senderId: String, message: String) {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(
                mapOf(
                    "senderId" to senderId,
                    "message" to message,
                    "timestamp" to FieldValue.serverTimestamp()
                )
            )
            .await()
    }
}
