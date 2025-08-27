package com.example.essence

import android.content.Intent // Import Intent
import android.os.Bundle
import android.util.Log
// import android.widget.TextView // Only if you still have dashboard cards in this activity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView // Import
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

        bottomNavigationView = findViewById(R.id.bottomNavigation) // Use the ID from your XML


        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = PropertyAdapter(
            propertyList,
            onApprove = { property -> updatePropertyStatus(property, "approved") },
            onReject = { property -> updatePropertyStatus(property, "rejected") },
            onRequestChange = { property -> updatePropertyStatus(property, "changes_requested") }
        )
        recyclerView.adapter = adapter

        db = FirebaseFirestore.getInstance()
        loadPropertiesForList()

        bottomNavigationView.selectedItemId = R.id.nav_properties

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_properties-> {
                    true
                }
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
        // ... your existing code ...
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

                if (itemsToProcess == 0) {
                    propertyList.clear()
                    adapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

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

    private fun updatePropertyStatus(property: Property, newStatus: String) {
        if (property.propertyId.isBlank()) {
            Toast.makeText(this, "Error: Property ID is missing. Cannot update.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("properties").document(property.propertyId)
            .update("status", newStatus)
            .addOnSuccessListener {
                val displayName = property.description.takeIf { it.isNotBlank() }?.let {
                    if (it.length > 30) it.take(30) + "..." else it
                } ?: "Property ID ${property.propertyId}"
                Toast.makeText(this, "'$displayName' status updated to $newStatus.", Toast.LENGTH_SHORT).show()

                val index = propertyList.indexOfFirst { it.propertyId == property.propertyId }
                if (index != -1) {
                    propertyList[index].status = newStatus
                    adapter.notifyItemChanged(index)
                }
            }
            .addOnFailureListener { e ->
                val displayName = property.description.takeIf { it.isNotBlank() }?.let {
                    if (it.length > 30) it.take(30) + "..." else it
                } ?: "Property ID ${property.propertyId}"
                Toast.makeText(this, "Error updating status for '$displayName': ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    override fun onResume() {
        super.onResume()
        bottomNavigationView.selectedItemId = R.id.nav_properties
    }
}

