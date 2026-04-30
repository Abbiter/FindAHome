package com.example.nestore_15.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nestore_15.R
import com.example.nestore_15.data.model.ChatMessage

class ChatMessageAdapter(
    private val currentUserId: String
) : RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

    private val items = mutableListOf<ChatMessage>()

    class MessageViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        ) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(parent)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = items[position]
        val prefix = if (item.senderId == currentUserId) "You: " else "Them: "
        holder.messageText.text = prefix + item.message
    }

    override fun getItemCount(): Int = items.size

    fun submit(messages: List<ChatMessage>) {
        items.clear()
        items.addAll(messages)
        notifyDataSetChanged()
    }
}
