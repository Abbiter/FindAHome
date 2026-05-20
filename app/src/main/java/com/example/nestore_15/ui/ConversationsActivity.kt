package com.example.nestore_15.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
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
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ConversationsActivity : AppCompatActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val chatRepository = ChatRepository()
    private var observeJob: Job? = null

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
        val emptyScroll = findViewById<ScrollView>(R.id.conversationsEmptyScroll)
        val errorText = findViewById<TextView>(R.id.tvConversationsError)
        val browseButton = findViewById<MaterialButton>(R.id.btnBrowseHomes)
        val refreshButton = findViewById<MaterialButton>(R.id.btnRefreshInbox)

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

        browseButton.setOnClickListener { finish() }
        refreshButton.setOnClickListener { startObserving(currentUserId, adapter, recycler, emptyScroll, errorText) }

        startObserving(currentUserId, adapter, recycler, emptyScroll, errorText)
    }

    private fun startObserving(
        currentUserId: String,
        adapter: ConversationAdapter,
        recycler: RecyclerView,
        emptyScroll: ScrollView,
        errorText: TextView
    ) {
        observeJob?.cancel()
        errorText.visibility = View.GONE
        observeJob = lifecycleScope.launch {
            runCatching {
                chatRepository.observeConversations(currentUserId).collect { conversations ->
                    adapter.submit(conversations)
                    val isEmpty = conversations.isEmpty()
                    emptyScroll.visibility = if (isEmpty) View.VISIBLE else View.GONE
                    recycler.visibility = if (isEmpty) View.GONE else View.VISIBLE
                    errorText.visibility = View.GONE
                }
            }.onFailure { err ->
                if (err is CancellationException) return@onFailure
                recycler.visibility = View.GONE
                emptyScroll.visibility = View.GONE
                errorText.visibility = View.VISIBLE
                val message = when ((err.cause as? FirebaseFirestoreException)?.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                        getString(R.string.conversations_load_error)
                    else -> err.message ?: getString(R.string.conversations_load_error)
                }
                errorText.text = message
            }
        }
    }

    private fun setupBackNavigation() {
        val toolbar = findViewById<Toolbar>(R.id.secondaryToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
}
