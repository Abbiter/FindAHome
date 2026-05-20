package com.example.nestore_15.notifications

import com.example.nestore_15.data.model.NotificationType
import com.example.nestore_15.data.repository.ChatRepository
import com.example.nestore_15.data.repository.UserRepository

class ChatMessageNotifier(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
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

        recipients.forEach { recipientId ->
            notificationHelper.notifyUser(
                userId = recipientId,
                title = "New message",
                message = "$senderLabel: $preview",
                type = NotificationType.CHAT,
                subtitle = propertyLabel
            )
        }
    }
}
