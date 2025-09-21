package com.example.essence

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class PropertyDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_detail)

        val property = intent.getParcelableExtra<Property>("property")

        if (property == null) {
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

        btnContact.setOnClickListener {
            Toast.makeText(this, "Contact seller: ${property.sellerEmail}", Toast.LENGTH_SHORT).show()
        }

        btnSave.setOnClickListener {
            SavedPropertiesManager.addProperty(property)
            Toast.makeText(this, "Property saved!", Toast.LENGTH_SHORT).show()
        }

        btnUnlock.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            paymentLauncher.launch(intent)
        }

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home  // highlight home by default

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, BuyerMainActivity::class.java))
                    true
                }
                R.id.nav_saved -> {
                    startActivity(Intent(this, SavedActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
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
            Toast.makeText(this, "Unlocked advanced view for property!", Toast.LENGTH_LONG).show()
            // TODO: You can redirect to a premium detail page or show hidden fields here
        } else {
            Toast.makeText(this, "Payment failed. Could not unlock.", Toast.LENGTH_LONG).show()
        }
    }
}
