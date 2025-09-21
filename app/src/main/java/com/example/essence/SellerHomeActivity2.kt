package com.example.essence

import NotificationHelper
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SellerHomeActivity2 : AppCompatActivity() {

    private lateinit var tvWelcomeMessage: TextView
    private lateinit var btnAddNewProperty: Button
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var notificationIcon: ImageView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_home_new)

        NotificationHelper.createNotificationChannel(this)
        tvWelcomeMessage = findViewById(R.id.tvWelcome)
        btnAddNewProperty = findViewById(R.id.btnAddNewProperty)
        notificationIcon = findViewById(R.id.notification_icon) // 🔔 bell icon

        bottomNavigationView = findViewById(R.id.bottomNavigation)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name") ?: "User"
                        tvWelcomeMessage.text = "Welcome, $name 👋"
                    }
                }
                .addOnFailureListener {
                    tvWelcomeMessage.text = "Welcome 👋"
                }
        }

        // Add Property button click
        btnAddNewProperty.setOnClickListener {
            val intent = Intent(this, UploadPropertyActivity::class.java)
            startActivity(intent)
        }

        // 🔔 Notification bell click - fetch latest admin message
        notificationIcon.setOnClickListener {
            db.collection("admin_messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val latestMessage = documents.documents[0].getString("message") ?: "No message"
                        NotificationHelper.showNotification(
                            this,
                            "Admin Message",
                            latestMessage
                        )
                    } else {
                        Toast.makeText(this, "No admin messages found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch admin message", Toast.LENGTH_SHORT).show()
                }
        }

// Real-time listener for admin messages
        db.collection("admin_messages")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                if (snapshots != null && !snapshots.isEmpty) {
                    val latestMessage = snapshots.documents[0].getString("message") ?: "No message"
                    NotificationHelper.showNotification(this, "Admin Message", latestMessage)
                }
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
                    val profileIntent = Intent(this, ProfileActivity::class.java)
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
