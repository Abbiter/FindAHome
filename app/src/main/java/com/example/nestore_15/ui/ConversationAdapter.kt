package com.example.nestore_15.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nestore_15.R
import com.example.nestore_15.data.model.ConversationSummary
import com.example.nestore_15.data.util.loadListingImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversationAdapter(
    private val currentUserId: String,
    private val onConversationClick: (ConversationSummary) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

    private val items = mutableListOf<ConversationSummary>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumb: ImageView = view.findViewById(R.id.ivConversationThumb)
        val name: TextView = view.findViewById(R.id.tvConversationName)
        val property: TextView = view.findViewById(R.id.tvConversationProperty)
        val lastMessage: TextView = view.findViewById(R.id.tvConversationLastMessage)
        val time: TextView = view.findViewById(R.id.tvConversationTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.otherParticipantName(currentUserId).ifBlank { "Unknown user" }
        holder.property.text = item.propertyTitle.ifBlank { "Property conversation" }
        holder.lastMessage.text = item.lastMessage.ifBlank { "No messages yet" }
        holder.time.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.lastUpdated))
        holder.thumb.loadListingImage(item.propertyImageUrl)
        holder.itemView.setOnClickListener { onConversationClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun submit(data: List<ConversationSummary>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }
}
