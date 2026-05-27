package com.example.nestore_15.notifications

import com.example.nestore_15.data.model.NotificationType
import com.example.nestore_15.data.repository.ChatRepository
import com.example.nestore_15.data.repository.UserRepository
import com.example.nestore_15.data.session.SessionManager

class ChatMessageNotifier(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val notificationHelper: AppNotificationHelper
) {

    suspend fun notifyRecipients(
        conversationId: String,
        senderId: String,
        messagePreview: String
    ) {
        val conversation = chatRepository.getConversation(conversationId) ?: return
        val recipients = conversation.participants.filter { it.isNotBlank() && it != senderId }
        val senderLabel = userRepository.getUser(senderId)?.displayNameOrEmail() ?: "Someone"
        val propertyLabel = conversation.propertyTitle.ifBlank { "a listing" }
        val preview = messagePreview.take(120)
        val activeUserId = sessionManager.getCurrentUserId()

        recipients.forEach { recipientId ->
            if (recipientId != activeUserId) return@forEach
            notificationHelper.notifyUser(
                userId = recipientId,
                dedupKey = "chat:$conversationId:$senderId:${preview.hashCode()}",
                title = "New message",
                message = "$senderLabel: $preview",
                type = NotificationType.CHAT,
                subtitle = propertyLabel
            )
        }
    }
}
