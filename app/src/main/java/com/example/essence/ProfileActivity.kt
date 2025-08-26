package com.example.essence

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var ivProfile: ImageView
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_user)

        // Init Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Bind views
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        tvRole = findViewById(R.id.tvRole)
        ivProfile = findViewById(R.id.ivProfileImage)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Fetch user data from Firestore
            firestore.collection("Users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name") ?: "N/A"
                        val email = document.getString("email") ?: "N/A"
                        val role = document.getString("role") ?: "buyer"
                        val photoUrl = document.getString("photoUrl")

                        // Set UI
                        tvName.text = name
                        tvEmail.text = email
                        tvRole.text = role

                        if (!photoUrl.isNullOrEmpty()) {
                            Glide.with(this).load(photoUrl).into(ivProfile)
                        }

                        // Switch bottom nav menu
                        if (role.equals("seller", ignoreCase = true)) {
                            bottomNavigation.menu.clear()
                            bottomNavigation.inflateMenu(R.menu.seller_bottom_nav_menu)
                        } else {
                            bottomNavigation.menu.clear()
                            bottomNavigation.inflateMenu(R.menu.buyer_bottom_nav_menu)
                        }

                        // Handle bottom nav clicks
                        bottomNavigation.setOnItemSelectedListener { item ->
                            when (item.itemId) {
                                R.id.nav_home -> {
                                    Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
                                    true
                                }
                                R.id.nav_profile -> {
                                    // Already on profile
                                    true
                                }
                                else -> false
                            }
                        }

                    } else {
                        Toast.makeText(this, "No profile data found!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
