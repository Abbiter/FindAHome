package com.example.nestore_15.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nestore_15.R
import com.example.nestore_15.data.model.Property
import com.example.nestore_15.data.util.ListingImageResolver
import com.example.nestore_15.data.util.loadListingImage
import com.example.nestore_15.data.model.PropertyStatus
import com.google.android.material.button.MaterialButton
import java.util.Locale

class ProviderManagePropertiesAdapter(
    private val onEdit: (Property) -> Unit,
    private val onDelete: (Property) -> Unit,
    private val onSetStatus: (Property, PropertyStatus) -> Unit
) : RecyclerView.Adapter<ProviderManagePropertiesAdapter.VH>() {

    private val items = mutableListOf<Property>()

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivManagePropertyImage)
        val title: TextView = view.findViewById(R.id.tvManagePropertyTitle)
        val price: TextView = view.findViewById(R.id.tvManagePropertyPrice)
        val status: TextView = view.findViewById(R.id.tvManagePropertyStatus)
        val menu: ImageButton = view.findViewById(R.id.btnManagePropertyMenu)
        val edit: MaterialButton = view.findViewById(R.id.btnManageEdit)
        val delete: MaterialButton = view.findViewById(R.id.btnManageDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_provider_manage_property, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        val ctx = holder.itemView.context
        holder.title.text = p.title
        holder.price.text = ctx.getString(R.string.listing_price_monthly, formatPrice(p.priceBwp))
        holder.status.text = statusLabel(p.availabilityStatus)

        holder.image.loadListingImage(ListingImageResolver.primaryFromList(p.imageUrls))

        holder.edit.setOnClickListener { onEdit(p) }
        holder.delete.setOnClickListener { onDelete(p) }
        holder.menu.setOnClickListener { anchor ->
            PopupMenu(ctx, anchor).apply {
                menu.add(0, 1, 0, "Set available")
                menu.add(0, 2, 0, "Set pending")
                menu.add(0, 3, 0, "Set rented")
                setOnMenuItemClickListener { mi ->
                    when (mi.itemId) {
                        1 -> onSetStatus(p, PropertyStatus.AVAILABLE)
                        2 -> onSetStatus(p, PropertyStatus.PENDING)
                        3 -> onSetStatus(p, PropertyStatus.RENTED)
                        else -> return@setOnMenuItemClickListener false
                    }
                    true
                }
                show()
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(list: List<Property>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    private fun formatPrice(price: Double): String {
        return if (price % 1.0 == 0.0) price.toInt().toString()
        else String.format(Locale.getDefault(), "%.2f", price)
    }

    private fun statusLabel(s: PropertyStatus): String = when (s) {
        PropertyStatus.AVAILABLE -> "Available"
        PropertyStatus.PENDING -> "Pending"
        PropertyStatus.RENTED -> "Rented"
    }
}
