package com.example.nestore_15.data.model

data class ChatMessage(
    val id: String,
    val senderId: String,
    val message: String,
    val timestamp: Long
)
