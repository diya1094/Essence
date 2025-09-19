package com.example.essence

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_home_new)

        NotificationHelper.createNotificationChannel(this)
        tvWelcomeMessage = findViewById(R.id.tvWelcome)
        btnAddNewProperty = findViewById(R.id.btnAddNewProperty)

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

        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_search -> {

                    Toast.makeText(this, "Search clicked (Not Implemented)", Toast.LENGTH_SHORT).show()
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
