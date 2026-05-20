package com.example.nestore_15.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.nestore_15.data.model.ConversationSummary
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.ui.screens.ConversationsScreen
import com.example.nestore_15.ui.theme.FindAHomeTheme
import com.example.nestore_15.viewmodel.ConversationsViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ConversationsActivity : ComponentActivity() {

    private val sessionManager by lazy { com.example.nestore_15.data.session.SessionManager(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val currentUserId = sessionManager.getCurrentUserId()
        if (currentUserId.isNullOrBlank()) {
            Toast.makeText(this, "Please log in to view chats", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val viewModel: ConversationsViewModel by viewModels {
            ConversationsViewModel.factory(currentUserId)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            FindAHomeTheme {
                ConversationsScreen(
                    uiState = uiState,
                    currentUserId = currentUserId,
                    onBack = { finish() },
                    onConversationClick = ::openChat,
                    onBrowseListings = { navigateBrowse() },
                    onRefresh = { viewModel.refresh() }
                )
            }
        }
    }

    private fun openChat(conversation: ConversationSummary) {
        val currentUserId = sessionManager.getCurrentUserId().orEmpty()
        val otherId = if (currentUserId == conversation.studentId) {
            conversation.providerId
        } else {
            conversation.studentId
        }
        startActivity(
            Intent(this, ChatActivity::class.java).apply {
                putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversation.id)
                putExtra(ChatActivity.EXTRA_PROPERTY_ID, conversation.propertyId)
                putExtra(ChatActivity.EXTRA_PROPERTY_TITLE, conversation.propertyTitle)
                putExtra(ChatActivity.EXTRA_PROPERTY_IMAGE_URL, conversation.propertyImageUrl)
                putExtra(ChatActivity.EXTRA_OWNER_ID, otherId)
                putExtra(ChatActivity.EXTRA_CURRENT_USER_ID, currentUserId)
                putExtra(ChatActivity.EXTRA_RETURN_TO_PROPERTY, false)
            }
        )
    }

    private fun navigateBrowse() {
        lifecycleScope.launch {
            when (sessionManager.userRole.first()) {
                UserRole.PROVIDER -> {
                    startActivity(Intent(this@ConversationsActivity, ProviderHomeActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    })
                }
                else -> finish()
            }
        }
    }
}
