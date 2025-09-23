package com.example.essence

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BuyerSavedAdapter(private val propertyList: List<Property>) :
    RecyclerView.Adapter<BuyerSavedAdapter.PropertyViewHolder>() {

    class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val propertyImage: ImageView = itemView.findViewById(R.id.propertyImage)
        val propertyTitle: TextView = itemView.findViewById(R.id.propertyTitle)
        val propertyPrice: TextView = itemView.findViewById(R.id.propertyPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_buyer_saved_adapter, parent, false)
        return PropertyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = propertyList[position]
        holder.propertyTitle.text = property.title
        holder.propertyPrice.text = "₹${property.price}"

        // Load property image using Glide (NEW CODE)
        val imageUrl = property.propertyImageUrls?.firstOrNull()
        if (!imageUrl.isNullOrBlank()) {
            Glide.with(holder.propertyImage.context)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_property_image)
                .error(R.drawable.ic_property_image)
                .into(holder.propertyImage)
        } else {
            holder.propertyImage.setImageResource(R.drawable.ic_property_image)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, PropertyDetailActivity::class.java)
            intent.putExtra("property", property) // pass the full object!
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = propertyList.size
}
