package com.example.nestore_15.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nestore_15.R
import com.example.nestore_15.data.repository.ChatRepository
import com.example.nestore_15.viewmodel.ChatUiState
import com.example.nestore_15.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private val chatRepository = ChatRepository()
    private val viewModel: ChatViewModel by viewModels { ChatViewModel.factory(chatRepository) }
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var adapter: ChatMessageAdapter

    private var chatId: String = ""
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat)
        setupBackNavigation()

        currentUserId = intent.getStringExtra(EXTRA_CURRENT_USER_ID).orEmpty()
        val listingId = intent.getStringExtra(EXTRA_LISTING_ID).orEmpty()
        val ownerId = intent.getStringExtra(EXTRA_OWNER_ID).orEmpty()

        if (currentUserId.isEmpty() || listingId.isEmpty() || ownerId.isEmpty()) {
            Toast.makeText(this, "Missing chat details", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendMessageButton)

        adapter = ChatMessageAdapter(currentUserId)
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.adapter = adapter

        lifecycleScope.launch {
            runCatching {
                chatId = chatRepository.getOrCreateChat(
                    listingId = listingId,
                    currentUserId = currentUserId,
                    ownerId = ownerId
                )
            }.onSuccess {
                viewModel.observeMessages(chatId)
            }.onFailure {
                Toast.makeText(this@ChatActivity, "Unable to open chat", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        sendButton.setOnClickListener {
            if (chatId.isBlank()) return@setOnClickListener
            viewModel.sendMessage(chatId, currentUserId, messageInput.text.toString())
            messageInput.text?.clear()
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                ChatUiState.Loading -> Unit
                is ChatUiState.Success -> {
                    adapter.submit(state.messages)
                    messagesRecyclerView.scrollToPosition((state.messages.size - 1).coerceAtLeast(0))
                }
                ChatUiState.Error -> {
                    Toast.makeText(this, "Chat connection error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupBackNavigation() {
        val toolbar = findViewById<Toolbar>(R.id.secondaryToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    companion object {
        const val EXTRA_LISTING_ID = "extra_listing_id"
        const val EXTRA_OWNER_ID = "extra_owner_id"
        const val EXTRA_CURRENT_USER_ID = "extra_current_user_id"
    }
}
