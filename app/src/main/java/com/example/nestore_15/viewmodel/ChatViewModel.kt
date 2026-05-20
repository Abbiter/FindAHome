package com.example.nestore_15.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.ChatMessage
import com.example.nestore_15.data.repository.ChatRepository
import com.example.nestore_15.data.repository.UserRepository
import com.example.nestore_15.notifications.AppNotificationHelper
import com.example.nestore_15.notifications.ChatMessageNotifier
import com.example.nestore_15.ui.screens.toProviderProfileUi
import com.example.nestore_15.ui.screens.toStudentProfileUi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ChatContactUi(
    val label: String,
    val name: String,
    val detailLine: String,
    val phoneLine: String
)

sealed class ChatUiState {
    data object Loading : ChatUiState()
    data class Success(val messages: List<ChatMessage>) : ChatUiState()
    data object Error : ChatUiState()
}

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val chatMessageNotifier: ChatMessageNotifier
) : ViewModel() {

    private val _uiState = MutableLiveData<ChatUiState>(ChatUiState.Loading)
    val uiState: LiveData<ChatUiState> = _uiState

    private val _contact = MutableLiveData<ChatContactUi?>()
    val contact: LiveData<ChatContactUi?> = _contact

    fun loadContact(conversationId: String, currentUserId: String) {
        viewModelScope.launch {
            val conversation = chatRepository.getConversation(conversationId) ?: return@launch
            val otherId = if (currentUserId == conversation.studentId) {
                conversation.providerId
            } else {
                conversation.studentId
            }
            val user = userRepository.getUser(otherId) ?: return@launch
            _contact.value = if (currentUserId == conversation.studentId) {
                val p = user.toProviderProfileUi()
                ChatContactUi(
                    label = "Property host",
                    name = p.displayName,
                    detailLine = listOf(p.businessName, p.email, p.contactAddress)
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
                    phoneLine = p.phone
                )
            } else {
                val s = user.toStudentProfileUi()
                ChatContactUi(
                    label = "Student",
                    name = s.displayName,
                    detailLine = listOf(s.institution, s.email)
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
                    phoneLine = s.phone
                )
            }
        }
    }

    fun observeMessages(chatId: String) {
        _uiState.value = ChatUiState.Loading
        viewModelScope.launch {
            runCatching {
                chatRepository.observeMessages(chatId).collectLatest { messages ->
                    _uiState.value = ChatUiState.Success(messages)
                }
            }.onFailure {
                _uiState.value = ChatUiState.Error
            }
        }
    }

    fun sendMessage(chatId: String, senderId: String, message: String) {
        val clean = message.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            runCatching {
                chatRepository.sendMessage(chatId, senderId, clean)
                chatMessageNotifier.notifyRecipients(chatId, senderId, clean)
            }
        }
    }

    companion object {
        fun factory(appContext: Context): ViewModelProvider.Factory {
            val helper = AppNotificationHelper(appContext)
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatViewModel(
                        chatRepository = ChatRepository(),
                        userRepository = UserRepository(),
                        chatMessageNotifier = ChatMessageNotifier(
                            ChatRepository(),
                            UserRepository(),
                            helper
                        )
                    ) as T
                }
            }
        }
    }
}
