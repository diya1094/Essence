package com.example.essence

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class SellerListingItemAdapter(private val propertyList: List<Property>) :
    RecyclerView.Adapter<SellerListingItemAdapter.PropertyViewHolder>() {

    class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val propertyImage: ImageView = itemView.findViewById(R.id.propertyImage)
        val propertyTitle: TextView = itemView.findViewById(R.id.propertyTitle)
        val propertyPrice: TextView = itemView.findViewById(R.id.propertyPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_seller_listing_item_adapter, parent, false)
        return PropertyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val currentProperty = propertyList[position]

        holder.propertyTitle.text = currentProperty.description
        holder.propertyPrice.text = "₹${currentProperty.price}"

        holder.itemView.setOnClickListener {
            Toast.makeText(
                holder.itemView.context,
                "Clicked: ${currentProperty.description}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount() = propertyList.size
}
