package com.example.essence

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AdminPropertyDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_property_detail)

        val property = intent.getParcelableExtra<Property>("property")

        val tvDetails = findViewById<TextView>(R.id.tvPropertyDetails)
        val ownersLayout = findViewById<LinearLayout>(R.id.ownersLayout)

        property?.let {
            tvDetails.text = """
                Title: ${it.title}
                Description: ${it.description}
                Price: ₹${it.price}
                Address: ${it.address}
                Year Built: ${it.yearBuilt}
                Type: ${it.propertyType}
                Status: ${it.status}
                Admin Message: ${it.adminMessage ?: "N/A"}
            """.trimIndent()

            // Clear any old views
            ownersLayout.removeAllViews()

            // Show owners
            if (it.jointOwners.isNotEmpty()) {
                for ((index, owner) in it.jointOwners.withIndex()) {
                    val ownerView = TextView(this)
                    ownerView.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 8, 0, 8)
                    }
                    ownerView.text = """
                        Owner ${index + 1}:
                        Name: ${owner.name}
                        Email: ${owner.email}
                        Phone: ${owner.phone}
                    """.trimIndent()
                    ownerView.textSize = 14f
                    ownersLayout.addView(ownerView)
                }
            } else {
                val noOwnerView = TextView(this)
                noOwnerView.text = "No owners available."
                ownersLayout.addView(noOwnerView)
            }
        }
    }
}