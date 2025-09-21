package com.example.essence

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class AdminActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PropertyAdapter
    private lateinit var db: FirebaseFirestore
    private val propertyList = mutableListOf<Property>()

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_property)

        bottomNavigationView = findViewById(R.id.bottomNavigation)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = PropertyAdapter(
            propertyList,
            onApprove = { property -> updatePropertyStatus(property, "approved") },
            onReject = { property, reason -> updatePropertyStatus(property, "rejected", reason) },
            onRequestChange = { property, reason -> updatePropertyStatus(property, "changes_requested", reason) }
        )
        recyclerView.adapter = adapter

        db = FirebaseFirestore.getInstance()
        loadPropertiesForList()

        bottomNavigationView.selectedItemId = R.id.nav_properties

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_properties -> true
                R.id.nav_stats -> {
                    val intent = Intent(this, AdminStatsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadPropertiesForList() {
        db.collection("properties")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("AdminActivity", "No properties found for list.")
                    propertyList.clear()
                    adapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                val fetchedProperties = mutableListOf<Property>()
                val itemsToProcess = documents.size()
                var itemsProcessedCount = 0

                for (doc in documents) {
                    val property = doc.toObject<Property>()
                    property.propertyId = doc.id

                    if (property.userId.isNotEmpty()) {
                        db.collection("users").document(property.userId).get()
                            .addOnSuccessListener { userDoc ->
                                if (userDoc.exists()) {
                                    property.sellerName = userDoc.getString("name") ?: "Unknown"
                                    property.sellerEmail = userDoc.getString("email") ?: "N/A"
                                } else {
                                    property.sellerName = "Seller Not Found"
                                    property.sellerEmail = "N/A"
                                }
                                fetchedProperties.add(property)
                            }
                            .addOnFailureListener { e ->
                                Log.w("AdminActivity", "Failed to fetch user ${property.userId}", e)
                                property.sellerName = "Error Fetching Seller"
                                property.sellerEmail = "N/A"
                                fetchedProperties.add(property)
                            }
                            .addOnCompleteListener {
                                itemsProcessedCount++
                                if (itemsProcessedCount == itemsToProcess) {
                                    propertyList.clear()
                                    propertyList.addAll(fetchedProperties)
                                    adapter.notifyDataSetChanged()
                                }
                            }
                    } else {
                        property.sellerName = "Seller ID Missing"
                        property.sellerEmail = "N/A"
                        fetchedProperties.add(property)
                        itemsProcessedCount++
                        if (itemsProcessedCount == itemsToProcess) {
                            propertyList.clear()
                            propertyList.addAll(fetchedProperties)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("AdminActivity", "Error loading properties: ${e.message}", e)
                Toast.makeText(this, "Error loading properties: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updatePropertyStatus(property: Property, newStatus: String, message: String? = null) {
        if (property.propertyId.isBlank()) {
            Toast.makeText(this, "Error: Property ID is missing. Cannot update.", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mutableMapOf<String, Any>("status" to newStatus)
        message?.let { updates["adminMessage"] = it }

        db.collection("properties").document(property.propertyId)
            .update(updates)
            .addOnSuccessListener {
                val displayName = property.description.takeIf { it.isNotBlank() }?.let {
                    if (it.length > 30) it.take(30) + "..." else it
                } ?: "Property ID ${property.propertyId}"

                Toast.makeText(this, "'$displayName' updated to $newStatus.", Toast.LENGTH_SHORT).show()

                val index = propertyList.indexOfFirst { it.propertyId == property.propertyId }
                if (index != -1) {
                    propertyList[index].status = newStatus
                    message?.let { propertyList[index].adminMessage = it }
                    adapter.notifyItemChanged(index)
                }

                val recipients = property.jointOwners.mapNotNull { it.email }.toMutableList()
                property.sellerEmail?.let { email ->
                    if (email.isNotBlank() && email != "N/A") {
                        recipients.add(email)
                    }
                }

                // === BEGIN TIMESTAMP ADDITION ===
                val msg = hashMapOf(
                    "propertyId" to property.propertyId,
                    "message" to (message ?: "Status updated to $newStatus"),
                    "recipients" to recipients,
                    "timestamp" to FieldValue.serverTimestamp() // <--- ADD THIS LINE
                )
                // === END TIMESTAMP ADDITION ===

                db.collection("adminMessages").add(msg)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Message sent to seller & owners!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                val displayName = property.description.takeIf { it.isNotBlank() }?.let {
                    if (it.length > 30) it.take(30) + "..." else it
                } ?: "Property ID ${property.propertyId}"

                Toast.makeText(this, "Error updating '$displayName': ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onResume() {
        super.onResume()
        bottomNavigationView.selectedItemId = R.id.nav_properties
    }
}
