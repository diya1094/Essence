package com.example.essence

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SellerHomeActivity2 : AppCompatActivity() {

    private lateinit var tvWelcomeMessage: TextView
    private lateinit var btnAddNewProperty: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_home_new)

        tvWelcomeMessage = findViewById(R.id.tvWelcome)
        btnAddNewProperty = findViewById(R.id.btnAddNewProperty)

        // Fetch user name from Firestore
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
    }
}
