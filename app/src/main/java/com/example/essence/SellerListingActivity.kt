package com.example.essence

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SellerListingActivity : AppCompatActivity() {

    private val TAG = "SellerListingActivity"

    private lateinit var recyclerView: RecyclerView
    private lateinit var propertyAdapter: SellerListingItemAdapter
    private val propertyList = mutableListOf<Property>()
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // TODO: IMPORTANT - Define the correct field name used in your "properties" collection to store the seller's User ID.
    private val SELLER_ID_FIELD_IN_PROPERTIES_COLLECTION = "userId"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_listing)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recyclerViewSellerProperties)
        recyclerView.layoutManager = LinearLayoutManager(this)
        propertyAdapter = SellerListingItemAdapter(propertyList)
        recyclerView.adapter = propertyAdapter

        bottomNavigationView = findViewById(R.id.bottomNavigation)

        fetchApprovedProperties()
        setupBottomNavigation()
    }

    private fun fetchApprovedProperties() {
        val currentFirebaseUser = auth.currentUser
        if (currentFirebaseUser == null) {
            Toast.makeText(this, "Please log in to see your listings.", Toast.LENGTH_LONG).show()
            Log.w(TAG, "Current user is null, cannot fetch listings.")
            // Consider redirecting to LoginActivity
            return
        }
        val currentUserId = currentFirebaseUser.uid
        Log.d(TAG, "Fetching approved properties for user ID: $currentUserId using field '$SELLER_ID_FIELD_IN_PROPERTIES_COLLECTION'")


        firestore.collection("properties")
            .whereEqualTo(SELLER_ID_FIELD_IN_PROPERTIES_COLLECTION, currentUserId)
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { documents ->
                if(documents.isEmpty){
                    Log.d(TAG, "No approved properties found.")
                    Toast.makeText(this, "No approved listings found.", Toast.LENGTH_SHORT).show()
                }
                propertyList.clear()
                for (doc in documents) {
                    try {
                        val property = doc.toObject(Property::class.java)
                        property.propertyId = doc.id // Store document ID
                        propertyList.add(property)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document to Property: ${doc.id}", e)
                    }
                }
                propertyAdapter.notifyDataSetChanged()
                Log.d(TAG, "Fetched ${propertyList.size} approved properties.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching approved properties: ", e)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNavigation() {
        try {
            bottomNavigationView.selectedItemId = R.id.nav_list
        } catch (e: Exception) {
            Log.e(TAG, "Error setting selected item ID for bottom navigation. Ensure R.id.nav_list exists in your menu.", e)
        }


        bottomNavigationView.setOnItemSelectedListener { item ->
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                Toast.makeText(this, "Please log in.", Toast.LENGTH_SHORT).show()
                return@setOnItemSelectedListener false // User not logged in
            }

            when (item.itemId) {
                R.id.nav_home -> {
                    Log.d(TAG, "Home clicked. Checking seller properties for user: $currentUserId")
                    firestore.collection("properties")
                        .whereEqualTo(SELLER_ID_FIELD_IN_PROPERTIES_COLLECTION, currentUserId)
                        .limit(1) // Optimization
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            val targetClass = if (querySnapshot.isEmpty) {
                                Log.i(TAG, "Seller $currentUserId has NO properties. Navigating to SellerHomeActivity2.")
                                SellerHomeActivity2::class.java
                            } else {
                                Log.i(TAG, "Seller $currentUserId HAS properties. Navigating to SellerHomeActivity.")
                                SellerHomeActivity::class.java
                            }
                            val intent = Intent(this, targetClass)
                            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error checking properties for home navigation", e)
                            Toast.makeText(this, "Error navigating home. Defaulting.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, SellerHomeActivity::class.java) // Default seller home
                            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                        }
                    true
                }
                R.id.nav_profile -> {
                    Log.d(TAG, "Profile clicked. Navigating to ProfileActivity.")
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    true
                }
                R.id.nav_list -> {
                    Log.d(TAG, "Listings tab clicked. Already on SellerListingActivity.")
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::bottomNavigationView.isInitialized) {
            try {
                bottomNavigationView.selectedItemId = R.id.nav_list
            } catch (e: Exception) {
                Log.e(TAG, "Error setting selected item ID in onResume.", e)
            }
        }
    }
}

