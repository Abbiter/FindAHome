package com.example.nestore_15.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.ConversationSummary
import com.example.nestore_15.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class ConversationsUiState {
    data object Loading : ConversationsUiState()
    data class Ready(val conversations: List<ConversationSummary>) : ConversationsUiState()
    data class Error(val message: String) : ConversationsUiState()
}

class ConversationsViewModel(
    private val currentUserId: String,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ConversationsUiState>(ConversationsUiState.Loading)
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init {
        observe()
    }

    fun refresh() {
        observe()
    }

    private fun observe() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.value = ConversationsUiState.Loading
            chatRepository.observeConversations(currentUserId)
                .catch { e ->
                    _uiState.value = ConversationsUiState.Error(
                        e.message ?: "Could not load conversations"
                    )
                }
                .collect { list ->
                    _uiState.value = ConversationsUiState.Ready(list)
                }
        }
    }

    companion object {
        fun factory(currentUserId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ConversationsViewModel(currentUserId, ChatRepository()) as T
                }
            }
    }
}
