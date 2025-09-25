package com.example.essence

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SellerListingItemAdapter(private val propertyList: List<Property>) :
    RecyclerView.Adapter<SellerListingItemAdapter.PropertyViewHolder>() {

    class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val propertyImage: ImageView = itemView.findViewById(R.id.propertyImage)
        val propertyTitle: TextView = itemView.findViewById(R.id.propertyTitle)
        val propertyPrice: TextView = itemView.findViewById(R.id.propertyPrice)
        val editBtn: Button = itemView.findViewById(R.id.btnEditProperty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_seller_listing_item_adapter, parent, false)
        return PropertyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val currentProperty = propertyList[position]

        holder.propertyTitle.text = currentProperty.title
        holder.propertyPrice.text = "₹${currentProperty.price}"

        // === Show first image from imageUrls if available ===
        val imageUrl = currentProperty.propertyImageUrls?.firstOrNull() // imageUrls: List<String>? must exist in Property
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(holder.propertyImage.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_property_image)
                .error(R.drawable.ic_property_image)
                .centerCrop()
                .into(holder.propertyImage)
        } else {
            holder.propertyImage.setImageResource(R.drawable.ic_property_image)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, SellerPropertyDetailActivity::class.java)
            intent.putExtra("propertyId", currentProperty.propertyId)
            holder.itemView.context.startActivity(intent)
        }

        holder.editBtn.isVisible =
            currentProperty.status == "rejected" || currentProperty.status == "changes_requested"
        holder.editBtn.setOnClickListener {
            val intent = Intent(holder.itemView.context, UploadPropertyActivity::class.java)
            intent.putExtra("propertyId", currentProperty.propertyId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = propertyList.size
}
