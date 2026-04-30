package com.example.nestore_15.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nestore_15.R
import com.example.nestore_15.data.model.Listing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProviderListingPreviewAdapter(
    private val onItemClick: (Listing) -> Unit
) : RecyclerView.Adapter<ProviderListingPreviewAdapter.ViewHolder>() {

    private val items = mutableListOf<Listing>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivListingImage)
        val title: TextView = view.findViewById(R.id.tvListingTitle)
        val location: TextView = view.findViewById(R.id.tvListingLocation)
        val price: TextView = view.findViewById(R.id.tvListingPrice)
        val status: TextView = view.findViewById(R.id.tvListingStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.title.text = item.title
        holder.location.text = item.location
        holder.price.text = context.getString(
            R.string.listing_price_monthly,
            formatPrice(item.priceBwp)
        )

        val status = resolveStatus(item)
        holder.status.isVisible = true
        holder.status.text = status.label
        holder.status.setTextColor(ContextCompat.getColor(context, status.textColor))
        holder.status.backgroundTintList = ContextCompat.getColorStateList(context, status.bgColor)

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .centerCrop()
            .into(holder.image)

        holder.itemView.alpha = 1f
        holder.itemView.isEnabled = true
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Listing>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    private fun formatPrice(price: Double): String {
        return if (price % 1.0 == 0.0) {
            price.toInt().toString()
        } else {
            String.format(Locale.getDefault(), "%.2f", price)
        }
    }

    private fun resolveStatus(listing: Listing): StatusUi {
        if (listing.isReserved) {
            return StatusUi("Rented", R.color.white, R.color.cobalt_blue)
        }
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val isPending = listing.availabilityDate.isNotBlank() && listing.availabilityDate > today
        return if (isPending) {
            StatusUi("Pending", R.color.midnight_navy, R.color.status_pending_orange)
        } else {
            StatusUi("Available", R.color.white, R.color.available_green)
        }
    }

    data class StatusUi(
        val label: String,
        val textColor: Int,
        val bgColor: Int
    )
}
