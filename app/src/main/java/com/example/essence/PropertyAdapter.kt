package com.example.essence

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class PropertyAdapter(
    private val properties: List<Property>,
    private val onApprove: (Property) -> Unit,
    private val onReject: (Property) -> Unit,
    private val onRequestChange: (Property) -> Unit
) : RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_property_adapter, parent, false) // Your item layout
        return PropertyViewHolder(view)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = properties[position]
        holder.bind(property)
    }

    override fun getItemCount() = properties.size

    inner class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Existing TextViews for property details
        private val tvSellerName: TextView = itemView.findViewById(R.id.tvSellerName)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvPropertyDesc)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPropertyPrice)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvPropertyAddress)
        private val tvYearBuilt: TextView = itemView.findViewById(R.id.tvYearBuilt)
        private val tvPropertyType: TextView = itemView.findViewById(R.id.tvPropertyType)
        private val tvPropertyStatus: TextView = itemView.findViewById(R.id.tvPropertyStatus)

        // TextView for the final status message (ensure ID matches XML)
        private val tvFinalStatusMessage: TextView = itemView.findViewById(R.id.tvFinalAdminStatusMessage)

        // LinearLayout containing the action buttons (ensure ID matches XML)
        private val layoutActionButtons: LinearLayout = itemView.findViewById(R.id.layoutAdminActionButtons)

        // Buttons
        private val approveBtn: Button = itemView.findViewById(R.id.btnApprove)
        private val rejectBtn: Button = itemView.findViewById(R.id.btnReject)
        private val requestChangeBtn: Button = itemView.findViewById(R.id.btnRequestChange)

        fun bind(property: Property) {
            Log.d("PropertyAdapter", "Binding property: ${property.propertyId}, Status: ${property.status}")

            tvSellerName.text = "Seller: ${property.sellerName.ifEmpty { "N/A" }}"
            tvDescription.text = "Description: ${property.description.ifEmpty { "N/A" }}"
            tvPrice.text = "Price: ₹${property.price.ifEmpty { "0" }}"
            tvAddress.text = "Address: ${property.address.ifEmpty { "N/A" }}"
            tvYearBuilt.text = "Year Built: ${property.yearBuilt.ifEmpty { "N/A" }}"
            tvPropertyType.text = "Type: ${property.propertyType.ifEmpty { "N/A" }}"
            tvPropertyStatus.text = "Status: ${property.status.capitalize()}"


            // Reset click listeners to prevent issues from view recycling
            approveBtn.setOnClickListener(null)
            rejectBtn.setOnClickListener(null)
            requestChangeBtn.setOnClickListener(null)

            when (property.status.toLowerCase()) { // Use toLowerCase for case-insensitive comparison
                "pending" -> {
                    layoutActionButtons.visibility = View.VISIBLE
                    tvFinalStatusMessage.visibility = View.GONE

                    approveBtn.setOnClickListener {
                        Log.d("PropertyAdapter", "Approve clicked for ${property.propertyId}")
                        onApprove(property)
                    }
                    rejectBtn.setOnClickListener {
                        Log.d("PropertyAdapter", "Reject clicked for ${property.propertyId}")
                        onReject(property)
                    }
                    requestChangeBtn.setOnClickListener {
                        Log.d("PropertyAdapter", "RequestChange clicked for ${property.propertyId}")
                        onRequestChange(property)
                    }
                }
                "approved" -> {
                    layoutActionButtons.visibility = View.GONE
                    tvFinalStatusMessage.visibility = View.VISIBLE
                    tvFinalStatusMessage.text = "Property Approved by Admin"
                    tvFinalStatusMessage.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
                }
                "rejected" -> {
                    layoutActionButtons.visibility = View.GONE
                    tvFinalStatusMessage.visibility = View.VISIBLE
                    tvFinalStatusMessage.text = "Property Rejected by Admin"
                    tvFinalStatusMessage.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
                }
                "changes_requested" -> {
                    layoutActionButtons.visibility = View.VISIBLE
                    tvFinalStatusMessage.visibility = View.GONE

                    approveBtn.setOnClickListener { onApprove(property) }
                    rejectBtn.setOnClickListener { onReject(property) }
                    requestChangeBtn.isEnabled = false
                    requestChangeBtn.text = "Changes Pending"
                }
                else -> {
                    // Default state for unknown statuses
                    layoutActionButtons.visibility = View.GONE
                    tvFinalStatusMessage.visibility = View.GONE
                    Log.w("PropertyAdapter", "Unknown property status: ${property.status} for ${property.propertyId}")
                }
            }
        }
    }
}

