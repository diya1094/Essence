package com.example.essence

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BuyerPropertyAdapter(
    private val propertyList: List<Property>,
    private val onItemClick: (Property) -> Unit
) : RecyclerView.Adapter<BuyerPropertyAdapter.PropertyViewHolder>() {

    inner class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.txtTitle)
        val price: TextView = itemView.findViewById(R.id.txtPrice)
        val image: ImageView = itemView.findViewById(R.id.imgProperty) // <-- Added!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_buyer_property_adapter, parent, false)
        return PropertyViewHolder(view)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = propertyList[position]
        holder.title.text = property.title
        holder.price.text = "₹${property.price}"

        // LOAD MAIN IMAGE WITH GLIDE
        val imageUrl = property.propertyImageUrls?.firstOrNull()
        if (!imageUrl.isNullOrBlank()) {
            Glide.with(holder.image.context)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_property_image)
                .error(R.drawable.ic_property_image)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.ic_property_image)
        }

        holder.itemView.setOnClickListener {
            onItemClick(property)
        }
    }

    override fun getItemCount(): Int = propertyList.size
}
