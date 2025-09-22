package com.example.essence

import NotificationHelper
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SellerHomeActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvRole: TextView
    private lateinit var btnAddNewProperty: Button
    private lateinit var listingsCount: TextView
    private lateinit var soldCount: TextView
    private val db = FirebaseFirestore.getInstance()

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var notificationIcon: ImageView // 🔔 bell icon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_home_exisiting)

        NotificationHelper.createNotificationChannel(this)
        tvWelcome = findViewById(R.id.tvWelcome)
        tvRole = findViewById(R.id.tvRole)
        btnAddNewProperty = findViewById(R.id.btnAddNewProperty)
        listingsCount = findViewById(R.id.listings_count)
        soldCount = findViewById(R.id.sold_count)

        bottomNavigationView = findViewById(R.id.bottomNavigation)

        notificationIcon = findViewById(R.id.notification_icon)

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        notificationIcon.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.email?.let { userEmail ->
                db.collection("adminMessages")
                    .whereArrayContains("recipients", userEmail)
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val doc = documents.documents[0]
                            val latestMessage = doc.getString("message") ?: "No message"
                            val propertyId = doc.getString("propertyId") ?: "Unknown Property"

                            val dialogMsg = "Notification: $latestMessage\n\nProperty ID: $propertyId"
                            androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Admin Notification")
                                .setMessage(dialogMsg)
                                .setPositiveButton("OK", null)
                                .show()
                        } else {
                            androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Admin Notification")
                                .setMessage("No admin messages found for you")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }
                    .addOnFailureListener { e ->
                        androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Admin Notification")
                            .setMessage("Failed to fetch admin message: ${e.message}")
                            .setPositiveButton("OK", null)
                            .show()
                    }
            }
        }


        currentUser?.email?.let { userEmail ->
            db.collection("adminMessages")
                .whereArrayContains("recipients", userEmail)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val latestMessage = documents.documents.last().getString("message") ?: "No message"
                        NotificationHelper.showNotification(
                            this,
                            "Admin Message",
                            latestMessage
                        )
                    } else {
                        Toast.makeText(this, "No admin messages found for you", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch admin message", Toast.LENGTH_SHORT).show()
                }

        }

        val userId = currentUser?.uid

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name") ?: "User"
                        val role = document.getString("role") ?: "Seller"

                        tvWelcome.text = "Welcome, $name 👋"
                        tvRole.text = role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    } else {
                        Log.d("SellerHomeActivity", "User document does not exist for UID: $userId")
                        tvWelcome.text = "Welcome 👋"
                        tvRole.text = "Seller"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("SellerHomeActivity", "Error fetching user profile", exception)
                    tvWelcome.text = "Welcome 👋"
                    tvRole.text = "Seller"
                }

            db.collection("properties")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        Log.e("SellerHomeActivity", "Error fetching property counts", error)
                        listingsCount.text = "0"
                        soldCount.text = "0"
                        return@addSnapshotListener
                    }

                    if (querySnapshot != null) {
                        var activeCount = 0
                        var soldCountValue = 0
                        for (doc in querySnapshot) {
                            val status = doc.getString("status") ?: "pending"
                            when (status.lowercase()) {
                                "approved" -> activeCount++
                                "sold" -> soldCountValue++
                            }
                        }
                        listingsCount.text = activeCount.toString()
                        soldCount.text = soldCountValue.toString()
                    } else {
                        listingsCount.text = "0"
                        soldCount.text = "0"
                    }
                }
        } else {
            Log.w("SellerHomeActivity", "Current user is null.")
            tvWelcome.text = "Welcome"
            tvRole.text = "Seller"
            listingsCount.text = "0"
            soldCount.text = "0"
        }

        btnAddNewProperty.setOnClickListener {
            val intent = Intent(this, UploadPropertyActivity::class.java)
            startActivity(intent)
        }

        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_list -> {
                    val profileIntent = Intent(this, SellerListingActivity::class.java)
                    startActivity(profileIntent)
                    true
                }
                R.id.nav_profile -> {
                    val profileIntent = Intent(this, ProfileActivity::class.java) // Assuming ProfileActivity exists
                    startActivity(profileIntent)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::bottomNavigationView.isInitialized) {
            bottomNavigationView.selectedItemId = R.id.nav_home
        }
    }
}