package com.example.essence

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

class PropertyDetailActivity : AppCompatActivity() {

    private lateinit var property: Property

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_detail)

        property = intent.getParcelableExtra<Property>("property")
            ?: run {
                Toast.makeText(this, "Property data not found!", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

        val tvTitle: TextView = findViewById(R.id.tvTitle)
        val tvDescription: TextView = findViewById(R.id.tvDescription)
        val tvAddress: TextView = findViewById(R.id.tvAddress)
        val tvPrice: TextView = findViewById(R.id.tvPrice)
        val tvYearBuilt: TextView = findViewById(R.id.tvYearBuilt)
        val tvPropertyType: TextView = findViewById(R.id.tvPropertyType)
        val tvPropertySize: TextView = findViewById(R.id.tvSize)
        val btnContact: Button = findViewById(R.id.btnContact)
        val btnSave: Button = findViewById(R.id.btnSave)
        val btnUnlock: Button = findViewById(R.id.btnUnlock)

        tvTitle.text = property.title
        tvDescription.text = property.description
        tvAddress.text = property.address
        tvPrice.text = "₹${property.price}"
        tvYearBuilt.text = property.yearBuilt
        tvPropertyType.text = property.propertyType
        tvPropertySize.text = property.propertySize

        // Image gallery
        val imageViewPager: ViewPager2 = findViewById(R.id.imageViewPager)
        val imageUrls = property.propertyImageUrls ?: emptyList()
        val imageAdapter = PropertyImageAdapter(imageUrls)
        imageViewPager.adapter = imageAdapter

        // === Show seller email as toast on contact button click ===
        btnContact.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Seller Email")
                .setMessage(property.sellerEmail ?: "Not available")
                .setPositiveButton("OK", null)
                .show()
        }

        btnSave.setOnClickListener {
            SavedPropertiesManager.addProperty(this, property)
            Toast.makeText(this, "Property saved!", Toast.LENGTH_SHORT).show()
        }

        btnUnlock.setOnClickListener {
            val intent = Intent(this, BuyerPaymentActivity::class.java)
            intent.putExtra("property", property) // pass property for payment status
            paymentLauncher.launch(intent)
        }

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, BuyerMainActivity::class.java)); true
                }

                R.id.nav_saved -> {
                    startActivity(Intent(this, SavedActivity::class.java)); true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java)); true
                }

                else -> false
            }
        }
    }

    private val paymentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK &&
            result.data?.getStringExtra("payment_status") == "success"
        ) {
            BuyerPaymentStatusManager.setPaymentDone(this, property.propertyId)
            Toast.makeText(this, "Unlocked advanced view for property!", Toast.LENGTH_LONG).show()
            val docIntent = Intent(this, BuyerDocumentActivity::class.java)
            docIntent.putExtra("property", property)
            startActivity(docIntent)
        } else {
            Toast.makeText(this, "Payment failed. Could not unlock.", Toast.LENGTH_LONG).show()
        }
    }
}
