package com.example.essence

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SellerHomeActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvRole: TextView
    private lateinit var btnAddNewProperty: Button
    private lateinit var listingsCount: TextView
    private lateinit var soldCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_home_exisiting)

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome)
        tvRole = findViewById(R.id.tvRole)
        btnAddNewProperty = findViewById(R.id.btnAddNewProperty)
        listingsCount = findViewById(R.id.listings_count)
        soldCount = findViewById(R.id.sold_count)

        // Fetch user data
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()

            // Fetch user profile info
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name") ?: "User"
                        val role = document.getString("role") ?: "Seller"

                        tvWelcome.text = "Welcome, $name 👋"
                        tvRole.text = role.replaceFirstChar { it.uppercase() }
                    }
                }
                .addOnFailureListener {
                    tvWelcome.text = "Welcome 👋"
                    tvRole.text = "Seller"
                }

            // Fetch property counts
            db.collection("properties")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    var activeCount = 0
                    var soldCountValue = 0

                    for (doc in querySnapshot) {
                        val status = doc.getString("status") ?: "pending"
                        when (status.lowercase()) {
                            "active" -> activeCount++
                            "sold" -> soldCountValue++
                        }
                    }

                    listingsCount.text = activeCount.toString()
                    soldCount.text = soldCountValue.toString()
                }
                .addOnFailureListener {
                    listingsCount.text = "0"
                    soldCount.text = "0"
                }
        }

        // Navigate to UploadPropertyActivity
        btnAddNewProperty.setOnClickListener {
            val intent = Intent(this, UploadPropertyActivity::class.java)
            startActivity(intent)
        }
    }
}
