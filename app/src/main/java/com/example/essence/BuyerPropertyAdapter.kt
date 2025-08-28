package com.example.essence

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BuyerPropertyAdapter(
    private val propertyList: List<Property>
) : RecyclerView.Adapter<BuyerPropertyAdapter.PropertyViewHolder>() {

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

        holder.propertyTitle.text = currentProperty.title
        holder.propertyPrice.text = "₹${currentProperty.price}"

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PropertyDetailActivity::class.java)
            intent.putExtra("property", currentProperty)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = propertyList.size
}
