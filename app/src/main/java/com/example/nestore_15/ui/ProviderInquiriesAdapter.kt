package com.example.nestore_15.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nestore_15.R
import com.example.nestore_15.data.model.Inquiry
import com.example.nestore_15.data.model.InquiryThreadStatus
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProviderInquiriesAdapter(
    private val onMarkPending: (Inquiry) -> Unit,
    private val onMarkResponded: (Inquiry) -> Unit
) : RecyclerView.Adapter<ProviderInquiriesAdapter.VH>() {

    private val items = mutableListOf<Inquiry>()

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val property: TextView = view.findViewById(R.id.tvInquiryProperty)
        val student: TextView = view.findViewById(R.id.tvInquiryStudent)
        val time: TextView = view.findViewById(R.id.tvInquiryTime)
        val message: TextView = view.findViewById(R.id.tvInquiryMessage)
        val status: TextView = view.findViewById(R.id.tvInquiryThreadStatus)
        val btnPending: MaterialButton = view.findViewById(R.id.btnInquiryPending)
        val btnResponded: MaterialButton = view.findViewById(R.id.btnInquiryResponded)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_provider_inquiry, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val inquiry = items[position]
        holder.property.text = inquiry.propertyTitle.ifBlank { "Property ${inquiry.propertyId}" }
        val name = inquiry.studentName.ifBlank { "Student" }
        holder.student.text = "From: $name"
        holder.message.text = inquiry.message
        holder.time.text = formatTime(inquiry.createdAt)
        holder.status.text = when (inquiry.inquiryStatus) {
            InquiryThreadStatus.PENDING -> "Pending"
            InquiryThreadStatus.RESPONDED -> "Responded"
        }

        holder.btnPending.setOnClickListener { onMarkPending(inquiry) }
        holder.btnResponded.setOnClickListener { onMarkResponded(inquiry) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(list: List<Inquiry>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    private fun formatTime(createdAt: Long): String {
        val fmt = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())
        return fmt.format(Date(createdAt))
    }
}
