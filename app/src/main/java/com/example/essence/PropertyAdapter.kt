package com.example.essence

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PropertyAdapter(
    private val properties: List<Property>,
    private val onApprove: (Property) -> Unit,
    private val onReject: (Property) -> Unit,
    private val onRequestChange: (Property) -> Unit
) : RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_property_adapter, parent, false)
        return PropertyViewHolder(view)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = properties[position]
        holder.bind(property)
    }

    override fun getItemCount() = properties.size

    inner class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val propertyName: TextView = itemView.findViewById(R.id.tvPropertyTitle)
        private val sellerName: TextView = itemView.findViewById(R.id.tvSellerName)
        private val approveBtn: Button = itemView.findViewById(R.id.btnApprove)
        private val rejectBtn: Button = itemView.findViewById(R.id.btnReject)
        private val requestChangeBtn: Button = itemView.findViewById(R.id.btnRequestChange)

        fun bind(property: Property) {
            propertyName.text = property.title
            sellerName.text = "Seller: ${property.sellerName}"

            approveBtn.setOnClickListener { onApprove(property) }
            rejectBtn.setOnClickListener { onReject(property) }
            requestChangeBtn.setOnClickListener { onRequestChange(property) }
        }
    }
}
