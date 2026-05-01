package com.example.nestore_15.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.nestore_15.data.model.Listing
import com.bumptech.glide.Glide
import com.example.nestore_15.R
import com.google.android.material.button.MaterialButton
import java.util.Locale

class ListingAdapter(
    private val onReserveClick: (Listing) -> Unit,
    private val onInquireClick: (Listing) -> Unit,
    private val onOpenDetail: (Listing) -> Unit
) :
    RecyclerView.Adapter<ListingAdapter.ViewHolder>() {

    private val items = mutableListOf<Listing>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.listingTitle)
        val price: TextView = view.findViewById(R.id.listingPrice)
        val location: TextView = view.findViewById(R.id.listingLocation)
        val availability: TextView = view.findViewById(R.id.listingAvailability)
        val statusBadge: TextView = view.findViewById(R.id.statusBadge)
        val image: ImageView = view.findViewById(R.id.listingImage)
        val btnReserve: MaterialButton = view.findViewById(R.id.btnReserve)
        val btnInquire: MaterialButton = view.findViewById(R.id.btnInquire)
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
        holder.price.text = context.getString(R.string.listing_price_monthly, formatPrice(item.priceBwp))
        holder.location.text = item.location
        holder.availability.text = context.getString(R.string.listing_available_on, item.availabilityDate)
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .centerCrop()
            .into(holder.image)

        holder.statusBadge.isVisible = item.isReserved
        holder.statusBadge.text = context.getString(R.string.listing_status_reserved)

        val blocked = item.isReserved
        holder.itemView.alpha = if (blocked) 0.85f else 1f
        holder.btnReserve.isEnabled = !blocked
        holder.btnInquire.isEnabled = !blocked

        holder.btnReserve.setOnClickListener {
            if (!blocked) onReserveClick(item)
        }
        holder.btnInquire.setOnClickListener {
            if (!blocked) onInquireClick(item)
        }
        holder.itemView.setOnClickListener {
            onOpenDetail(item)
        }
    }

    override fun getItemCount() = items.size

    override fun getItemId(position: Int): Long = items[position].id.hashCode().toLong()

    fun submitList(newList: List<Listing>) {
        val oldList = items.toList()
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size

            override fun getNewListSize(): Int = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].id == newList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }
        })

        items.clear()
        items.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    private fun formatPrice(price: Double): String {
        return if (price % 1.0 == 0.0) {
            price.toInt().toString()
        } else {
            String.format(Locale.getDefault(), "%.2f", price)
        }
    }

    init {
        setHasStableIds(true)
    }
}
