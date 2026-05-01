package com.example.nestore_15.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nestore_15.R
import com.example.nestore_15.data.repository.ChatRepository
import com.example.nestore_15.data.session.SessionManager
import kotlinx.coroutines.launch

class ConversationsActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val chatRepository = ChatRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversations)
        setupBackNavigation()

        val currentUserId = sessionManager.getCurrentUserId()
        if (currentUserId.isNullOrBlank()) {
            Toast.makeText(this, "Please log in to view chats", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val recycler = findViewById<RecyclerView>(R.id.rvConversations)
        val empty = findViewById<TextView>(R.id.tvConversationsEmpty)
        val adapter = ConversationAdapter(currentUserId) { conversation ->
            startActivity(
                Intent(this, ChatActivity::class.java).apply {
                    putExtra(ChatActivity.EXTRA_CONVERSATION_ID, conversation.id)
                    putExtra(ChatActivity.EXTRA_PROPERTY_ID, conversation.propertyId)
                    putExtra(ChatActivity.EXTRA_PROPERTY_TITLE, conversation.propertyTitle)
                    putExtra(ChatActivity.EXTRA_PROPERTY_IMAGE_URL, conversation.propertyImageUrl)
                    putExtra(ChatActivity.EXTRA_CURRENT_USER_ID, currentUserId)
                    putExtra(ChatActivity.EXTRA_RETURN_TO_PROPERTY, false)
                }
            )
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        lifecycleScope.launch {
            chatRepository.observeConversations(currentUserId).collect { conversations ->
                adapter.submit(conversations)
                empty.visibility = if (conversations.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupBackNavigation() {
        val toolbar = findViewById<Toolbar>(R.id.secondaryToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
}
