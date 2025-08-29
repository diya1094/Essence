package com.example.essence

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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
        val tvAddress: TextView = findViewById(R.id.tvAddress)
        val tvPrice: TextView = findViewById(R.id.tvPrice)
        val tvYearBuilt: TextView = findViewById(R.id.tvYearBuilt)
        val tvPropertyType: TextView = findViewById(R.id.tvPropertyType)

        val btnContact: Button = findViewById(R.id.btnContact)
        val btnSave: Button = findViewById(R.id.btnSave)
        val btnUnlock: Button = findViewById(R.id.btnUnlock)

        tvTitle.text = property.description
        tvAddress.text = property.address
        tvPrice.text = "₹${property.price}"
        tvYearBuilt.text = property.yearBuilt
        tvPropertyType.text = property.propertyType

        btnContact.setOnClickListener {
            Toast.makeText(this, "Contact seller: ${property.sellerEmail}", Toast.LENGTH_SHORT).show()
        }

        btnSave.setOnClickListener {
            Toast.makeText(this, "Property saved!", Toast.LENGTH_SHORT).show()
        }

        btnUnlock.setOnClickListener {
            Toast.makeText(this, "Unlocking advanced view...", Toast.LENGTH_SHORT).show()
        }
    }
}
