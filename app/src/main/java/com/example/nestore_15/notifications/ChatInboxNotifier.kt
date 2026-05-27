package com.example.nestore_15.notifications

import android.content.Context
import com.example.nestore_15.data.model.NotificationType
import com.example.nestore_15.data.preferences.ChatSeenStore
import com.example.nestore_15.data.repository.ChatRepository
import com.example.nestore_15.data.session.SessionManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * In-app (foreground) chat notifications.
 *
 * Note: this is NOT push (FCM). It works while the app process is alive and listening.
 */
class ChatInboxNotifier(
    private val context: Context,
    private val sessionManager: SessionManager,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val chatRepository: ChatRepository = ChatRepository(),
    private val seenStore: ChatSeenStore = ChatSeenStore(context),
    private val notificationHelper: AppNotificationHelper = AppNotificationHelper(context)
) {
    private var job: Job? = null
    private val messageListeners = LinkedHashMap<String, ListenerRegistration>()

    fun start(scope: CoroutineScope) {
        if (job != null) return
        job = scope.launch {
            val userId = sessionManager.getCurrentUserId().orEmpty()
            if (userId.isBlank()) return@launch

            chatRepository.observeConversations(userId).collectLatest { conversations ->
                val activeConversationIds = conversations.map { it.id }.toSet()

                // Remove listeners for conversations that no longer apply
                val removing = messageListeners.keys.filter { it !in activeConversationIds }
                removing.forEach { id ->
                    messageListeners.remove(id)?.remove()
                }

                // Add listeners for new conversations
                conversations.forEach { c ->
                    if (messageListeners.containsKey(c.id)) return@forEach
                    val registration = firestore.collection("conversations")
                        .document(c.id)
                        .collection("messages")
                        .orderBy("timestamp")
                        .limitToLast(1)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) return@addSnapshotListener
                            val doc = snapshot?.documents?.lastOrNull() ?: return@addSnapshotListener
                            val messageId = doc.id
                            val senderId = doc.getString("senderId").orEmpty()
                            val message = doc.getString("message").orEmpty()
                            val ts = (doc.getTimestamp("timestamp") ?: Timestamp.now()).toDate().time

                            // Ignore your own messages
                            if (senderId.isBlank() || senderId == userId) return@addSnapshotListener
                            if (message.isBlank()) return@addSnapshotListener

                            scope.launch {
                                val stillLoggedIn = sessionManager.getCurrentUserId().orEmpty() == userId
                                if (!stillLoggedIn) return@launch

                                val lastNotified = seenStore.lastNotifiedMessageId(userId, c.id)
                                if (lastNotified == messageId) return@launch

                                val ok = notificationHelper.notifyUser(
                                    userId = userId,
                                    dedupKey = "chat_inbox:${c.id}:$messageId:$ts",
                                    title = "New message",
                                    message = message.take(120),
                                    type = NotificationType.CHAT,
                                    subtitle = c.propertyTitle
                                )
                                if (ok) {
                                    seenStore.setLastNotifiedMessageId(userId, c.id, messageId)
                                }
                            }
                        }
                    messageListeners[c.id] = registration
                }
            }
        }
    }
}

