package com.example.essence

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
        setContentView(R.layout.activity_seller_property_detail)

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

                // === Tag: value for all details ===
                findViewById<TextView>(R.id.tvTitle).text = "Title: ${doc.getString("title") ?: ""}"
                findViewById<TextView>(R.id.tvDescription).text = "Description: ${doc.getString("description") ?: ""}"
                findViewById<TextView>(R.id.tvAddress).text = "Address: ${doc.getString("address") ?: ""}"
                findViewById<TextView>(R.id.tvPrice).text = "Price: ₹${doc.getString("price") ?: ""}"
                findViewById<TextView>(R.id.tvYearBuilt).text = "Age of Property(in years): ${doc.getString("yearBuilt") ?: ""}"
                findViewById<TextView>(R.id.tvPropertyType).text = "Type: ${doc.getString("propertyType") ?: ""}"
                findViewById<TextView>(R.id.tvSize).text = "Size(in sq.ft.): ${doc.getString("propertySize") ?: ""}"

                // Show images
                val imageUrls = doc.get("propertyImageUrls") as? List<String> ?: emptyList()
                val imagesLayout = findViewById<LinearLayout>(R.id.imagesLayout)
                imagesLayout.removeAllViews()
                for (url in imageUrls) {
                    val imageView = ImageView(this)
                    imageView.layoutParams = LinearLayout.LayoutParams(240, 180).apply {
                        setMargins(0, 0, 24, 0)
                    }
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(this).load(url)
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .into(imageView)
                    imageView.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    }
                    imagesLayout.addView(imageView)
                }

                // Show documents as buttons (admin style)
                val docsLayout = findViewById<LinearLayout>(R.id.docsLayout)
                docsLayout.removeAllViews()
                val docFields = listOf(
                    "Title Deed" to doc.getString("titleDeedUrl"),
                    "Encumbrance Certificate" to doc.getString("encumbranceUrl"),
                    "Mutation Document" to doc.getString("mutationUrl"),
                    "Property Tax Receipt" to doc.getString("propertyTaxUrl"),
                    "Possession Letter" to doc.getString("possessionUrl"),
                    "No Objection Certificate" to doc.getString("nocUrl"),
                    "Utility Bill" to doc.getString("utilityBillUrl")
                )
                var docAdded = false
                for ((label, url) in docFields) {
                    if (!url.isNullOrEmpty()) {
                        val btn = Button(this).apply {
                            text = "View $label"
                            setOnClickListener {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                startActivity(intent)
                            }
                        }
                        docsLayout.addView(btn)
                        docAdded = true
                    }
                }
                if (!docAdded) {
                    docsLayout.addView(TextView(this).apply { text = "No document files uploaded." })
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
