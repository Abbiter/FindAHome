package com.example.nestore_15.ui

import android.view.LayoutInflater
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nestore_15.R
import com.example.nestore_15.data.model.ChatMessage
import com.example.nestore_15.data.repository.ChatRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatMessageAdapter(
    private val currentUserId: String
) : RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>() {

    private val items = mutableListOf<ChatMessage>()

    class MessageViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        ) {
        val container: LinearLayout = itemView.findViewById(R.id.messageContainer)
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val messageTimestamp: TextView = itemView.findViewById(R.id.messageTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(parent)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = items[position]
        val system = item.senderId == ChatRepository.SYSTEM_SENDER_ID
        val mine = !system && item.senderId == currentUserId
        holder.container.gravity = when {
            system -> Gravity.CENTER_HORIZONTAL
            mine -> Gravity.END
            else -> Gravity.START
        }
        holder.messageText.text = item.message
        holder.messageText.setBackgroundResource(
            when {
                system -> R.drawable.bg_chat_bubble_received
                mine -> R.drawable.bg_chat_bubble_sent
                else -> R.drawable.bg_chat_bubble_received
            }
        )
        holder.messageText.setTextColor(
            when {
                system -> holder.itemView.context.getColor(R.color.deep_royal_text)
                mine -> holder.itemView.context.getColor(R.color.white)
                else -> holder.itemView.context.getColor(R.color.midnight_navy)
            }
        )
        holder.messageTimestamp.text =
            SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault()).format(Date(item.timestamp))
    }

    override fun getItemCount(): Int = items.size

    fun submit(messages: List<ChatMessage>) {
        items.clear()
        items.addAll(messages)
        notifyDataSetChanged()
    }
}
