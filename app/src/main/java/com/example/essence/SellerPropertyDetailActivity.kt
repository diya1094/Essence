package com.example.essence

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class SellerPropertyDetailActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_property_detail) // you need to create this layout!

        val propertyId = intent.getStringExtra("propertyId")
        if (propertyId.isNullOrEmpty()) {
            Toast.makeText(this, "Property ID missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db = FirebaseFirestore.getInstance()
        db.collection("properties").document(propertyId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Property data not found!", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                // Bind main property details
                findViewById<TextView>(R.id.tvTitle).text = doc.getString("title") ?: ""
                findViewById<TextView>(R.id.tvDescription).text = doc.getString("description") ?: ""
                findViewById<TextView>(R.id.tvAddress).text = doc.getString("address") ?: ""
                findViewById<TextView>(R.id.tvPrice).text = "₹" + (doc.getString("price") ?: "")
                findViewById<TextView>(R.id.tvYearBuilt).text = doc.getString("yearBuilt") ?: ""
                findViewById<TextView>(R.id.tvPropertyType).text = doc.getString("propertyType") ?: ""
                findViewById<TextView>(R.id.tvSize).text = doc.getString("propertySize") ?: ""

                // Show images/docs
                val imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList()
                val documentUrls = doc.get("documentUrls") as? List<String> ?: emptyList()

                val imagesLayout = findViewById<LinearLayout>(R.id.imagesLayout)
                imagesLayout.removeAllViews()
                for (url in imageUrls) {
                    val imageView = ImageView(this)
                    imageView.layoutParams = LinearLayout.LayoutParams(240, 180)
                    Glide.with(this).load(url).into(imageView)
                    imageView.setPadding(10, 10, 10, 10)
                    imagesLayout.addView(imageView)
                }

                val docsLayout = findViewById<LinearLayout>(R.id.docsLayout)
                docsLayout.removeAllViews()
                for (url in documentUrls) {
                    val docView = TextView(this)
                    docView.text = url
                    docView.setPadding(4, 4, 4, 4)
                    docsLayout.addView(docView)
                    // Could add a button or clickable link, or use a WebView
                }
            }
            .addOnFailureListener { e ->
                Log.e("SellerPropertyDetail", "Failed to load property: $e")
                Toast.makeText(this, "Error loading property.", Toast.LENGTH_SHORT).show()
                finish()
            }

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_list
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, SellerHomeActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_list -> true // already here
                else -> false
            }
        }
    }
}
