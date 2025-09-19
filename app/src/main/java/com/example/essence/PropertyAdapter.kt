package com.example.essence

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class PropertyAdapter(
    private val properties: List<Property>,
    private val onApprove: (Property) -> Unit,
    private val onReject: (Property, String) -> Unit,
    private val onRequestChange: (Property, String) -> Unit
) : RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_property_adapter, parent, false)
        return PropertyViewHolder(view)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = properties[position]
        holder.bind(property)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, AdminPropertyDetailActivity::class.java)
            intent.putExtra("property", property)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = properties.size

    inner class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSellerName: TextView = itemView.findViewById(R.id.tvSellerName)
        private val tvPropertyTitle: TextView = itemView.findViewById(R.id.tvPropertyTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvPropertyDesc)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPropertyPrice)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvPropertyAddress)
        private val tvYearBuilt: TextView = itemView.findViewById(R.id.tvYearBuilt)
        private val tvPropertyType: TextView = itemView.findViewById(R.id.tvPropertyType)
        private val tvPropertyStatus: TextView = itemView.findViewById(R.id.tvPropertyStatus)
        private val tvDetailsOwnersCount: TextView = itemView.findViewById(R.id.tvDetailsOwnersCount)
        private val layoutJointOwners: LinearLayout = itemView.findViewById(R.id.layoutJointOwners)

        private val tvFinalStatusMessage: TextView = itemView.findViewById(R.id.tvFinalAdminStatusMessage)
        private val layoutActionButtons: LinearLayout = itemView.findViewById(R.id.layoutAdminActionButtons)

        private val approveBtn: Button = itemView.findViewById(R.id.btnApprove)
        private val rejectBtn: Button = itemView.findViewById(R.id.btnReject)
        private val requestChangeBtn: Button = itemView.findViewById(R.id.btnRequestChange)

        fun bind(property: Property) {
            tvSellerName.text = "Seller: ${property.sellerName.ifEmpty { "N/A" }}"
            tvPropertyTitle.text = "Title: ${property.title.ifEmpty { "N/A" }}"
            tvDescription.text = "Description: ${property.description.ifEmpty { "N/A" }}"
            tvPrice.text = "Price: ₹${property.price.ifEmpty { "0" }}"
            tvAddress.text = "Address: ${property.address.ifEmpty { "N/A" }}"
            tvYearBuilt.text = "Year Built: ${property.yearBuilt.ifEmpty { "N/A" }}"
            tvPropertyType.text = "Type: ${property.propertyType.ifEmpty { "N/A" }}"
            tvDetailsOwnersCount.text = "Total Owners: ${property.jointOwners.size}"
            tvPropertyStatus.text = "Status: ${property.status.capitalize()}"

            when (property.status.lowercase()) {
                "pending" -> {
                    layoutActionButtons.visibility = View.VISIBLE
                    tvFinalStatusMessage.visibility = View.GONE

                    approveBtn.setOnClickListener { onApprove(property) }

                    rejectBtn.setOnClickListener {
                        showReasonDialog(itemView.context, "Reject") { reason ->
                            onReject(property, reason)
                        }
                    }

                    requestChangeBtn.setOnClickListener {
                        showReasonDialog(itemView.context, "Request Changes") { reason ->
                            onRequestChange(property, reason)
                        }
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
                    layoutActionButtons.visibility = View.GONE
                    tvFinalStatusMessage.visibility = View.VISIBLE
                    tvFinalStatusMessage.text = "Changes Requested by Admin"
                    tvFinalStatusMessage.setTextColor(ContextCompat.getColor(itemView.context, R.color.orange))
                }
            }
        }

        private fun showReasonDialog(context: Context, action: String, onSubmit: (String) -> Unit) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_request_changes, null)
            val input = dialogView.findViewById<EditText>(R.id.etReason)
            val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

            val dialog = AlertDialog.Builder(context)
                .setTitle("$action Property")
                .setView(dialogView)
                .create()

            btnSubmit.setOnClickListener {
                val reason = input.text.toString().ifEmpty { "No reason provided" }
                onSubmit(reason)
                dialog.dismiss()
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}
