package com.example.nestore_15.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nestore_15.R
import com.example.nestore_15.data.util.loadListingImage
import com.example.nestore_15.viewmodel.ChatUiState
import com.example.nestore_15.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private val viewModel: ChatViewModel by viewModels { ChatViewModel.factory() }
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var adapter: ChatMessageAdapter
    private lateinit var contactInfoBlock: LinearLayout

    private var conversationId: String = ""
    private var currentUserId: String = ""
    private var returnToProperty: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat)
        setupBackNavigation()

        currentUserId = intent.getStringExtra(EXTRA_CURRENT_USER_ID).orEmpty()
        val providedConversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID).orEmpty()
        val propertyTitle = intent.getStringExtra(EXTRA_PROPERTY_TITLE).orEmpty()
        val propertyImageUrl = intent.getStringExtra(EXTRA_PROPERTY_IMAGE_URL).orEmpty()
        returnToProperty = intent.getBooleanExtra(EXTRA_RETURN_TO_PROPERTY, false)

        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "Missing chat details", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendMessageButton)
        contactInfoBlock = findViewById(R.id.contactInfoBlock)

        adapter = ChatMessageAdapter(currentUserId)
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.adapter = adapter

        findViewById<TextView>(R.id.tvChatPropertyTitle).text = propertyTitle.ifBlank { "Property conversation" }
        findViewById<ImageView>(R.id.ivChatPropertyThumb).loadListingImage(propertyImageUrl)

        lifecycleScope.launch {
            if (providedConversationId.isNotBlank()) {
                conversationId = providedConversationId
                viewModel.loadContact(conversationId, currentUserId)
                viewModel.observeMessages(conversationId)
            } else {
                Toast.makeText(this@ChatActivity, "Conversation not found", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
        }

        sendButton.setOnClickListener {
            if (conversationId.isBlank()) return@setOnClickListener
            viewModel.sendMessage(conversationId, currentUserId, messageInput.text.toString())
            messageInput.text?.clear()
        }

        viewModel.contact.observe(this) { contact ->
            if (contact == null) {
                contactInfoBlock.visibility = View.GONE
                return@observe
            }
            contactInfoBlock.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tvChatContactLabel).text = contact.label
            findViewById<TextView>(R.id.tvChatContactName).text = contact.name
            findViewById<TextView>(R.id.tvChatContactDetail).apply {
                text = contact.detailLine
                visibility = if (contact.detailLine.isBlank()) View.GONE else View.VISIBLE
            }
            findViewById<TextView>(R.id.tvChatContactPhone).apply {
                text = contact.phoneLine
                visibility = if (contact.phoneLine.isBlank()) View.GONE else View.VISIBLE
            }
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
        toolbar.setNavigationOnClickListener { navigateUp() }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        navigateUp()
    }

    private fun navigateUp() {
        val propertyId = intent.getStringExtra(EXTRA_PROPERTY_ID).orEmpty()
        val providerId = intent.getStringExtra(EXTRA_OWNER_ID).orEmpty()
        val propertyTitle = intent.getStringExtra(EXTRA_PROPERTY_TITLE).orEmpty()
        val propertyImageUrl = intent.getStringExtra(EXTRA_PROPERTY_IMAGE_URL).orEmpty()
        val propertyLocation = intent.getStringExtra(EXTRA_PROPERTY_LOCATION).orEmpty()
        val propertyPrice = intent.getStringExtra(EXTRA_PROPERTY_PRICE).orEmpty()
        val propertyAvailability = intent.getStringExtra(EXTRA_PROPERTY_AVAILABILITY).orEmpty()

        if (returnToProperty && propertyId.isNotBlank()) {
            startActivity(
                Intent(this, StudentListingDetailActivity::class.java).apply {
                    putExtra(StudentListingDetailActivity.EXTRA_PROPERTY_ID, propertyId)
                    putExtra(StudentListingDetailActivity.EXTRA_PROVIDER_ID, providerId)
                    putExtra(StudentListingDetailActivity.EXTRA_PROPERTY_TITLE, propertyTitle)
                    putExtra(StudentListingDetailActivity.EXTRA_PROPERTY_IMAGE_URL, propertyImageUrl)
                    putExtra(StudentListingDetailActivity.EXTRA_PROPERTY_LOCATION, propertyLocation)
                    putExtra(StudentListingDetailActivity.EXTRA_PROPERTY_PRICE, propertyPrice)
                    putExtra(StudentListingDetailActivity.EXTRA_PROPERTY_AVAILABILITY, propertyAvailability)
                }
            )
            finish()
            return
        }
        if (!returnToProperty) {
            startActivity(Intent(this, ConversationsActivity::class.java))
            finish()
            return
        }
        finish()
    }

    companion object {
        const val EXTRA_CONVERSATION_ID = "extra_conversation_id"
        const val EXTRA_PROPERTY_ID = "extra_property_id"
        const val EXTRA_PROPERTY_TITLE = "extra_property_title"
        const val EXTRA_PROPERTY_IMAGE_URL = "extra_property_image_url"
        const val EXTRA_PROPERTY_LOCATION = "extra_property_location"
        const val EXTRA_PROPERTY_PRICE = "extra_property_price"
        const val EXTRA_PROPERTY_AVAILABILITY = "extra_property_availability"
        const val EXTRA_OWNER_ID = "extra_owner_id"
        const val EXTRA_CURRENT_USER_ID = "extra_current_user_id"
        const val EXTRA_RETURN_TO_PROPERTY = "extra_return_to_property"
    }
}
