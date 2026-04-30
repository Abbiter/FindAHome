package com.example.nestore_15.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.ChatMessage
import com.example.nestore_15.data.repository.ChatRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class ChatUiState {
    data object Loading : ChatUiState()
    data class Success(val messages: List<ChatMessage>) : ChatUiState()
    data object Error : ChatUiState()
}

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<ChatUiState>(ChatUiState.Loading)
    val uiState: LiveData<ChatUiState> = _uiState

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
            }
        }
    }

    companion object {
        fun factory(chatRepository: ChatRepository = ChatRepository()): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatViewModel(chatRepository) as T
                }
            }
    }
}
