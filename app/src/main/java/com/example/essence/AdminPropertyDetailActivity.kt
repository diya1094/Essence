package com.example.essence

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class AdminPropertyDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_property_detail)

        val property = intent.getParcelableExtra<Property>("property")
        val tvDetails = findViewById<TextView>(R.id.tvPropertyDetails)
        val ownersLayout = findViewById<LinearLayout>(R.id.ownersLayout)
        val filesLayout = findViewById<LinearLayout>(R.id.filesLayout)
        val imagesLayout = findViewById<LinearLayout>(R.id.imagesLayout)

        property?.let { it ->
            tvDetails.text = """
                Title: ${it.title}
                Description: ${it.description}
                Price: ₹${it.price}
                Address: ${it.address}
                Year Built: ${it.yearBuilt}
                Type: ${it.propertyType}
                Size: ${it.propertySize?.let { "$it sq ft" } ?: "N/A"}
                Status: ${it.status}
                Admin Message: ${it.adminMessage ?: "N/A"}
            """.trimIndent()

            // Owners
            ownersLayout.removeAllViews()
            if (!it.jointOwners.isNullOrEmpty()) {
                for ((index, owner) in it.jointOwners.withIndex()) {
                    val ownerView = TextView(this)
                    ownerView.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 8, 0, 8) }
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

            // --- Files block ---
            filesLayout.removeAllViews()
            val docFields = listOf(
                "Title Deed" to it.titleDeedUrl,
                "Non Dispute Affidavit" to it.nonDisputeUrl,
                "Encumbrance Certificate" to it.encumbranceUrl,
                "Property Tax Receipt" to it.propertyTaxUrl,
                "Mutation Document" to it.mutationUrl,
                "Possession Letter" to it.possessionUrl,
                "No Objection Certificate" to it.nocUrl,
                "Utility Bill" to it.utilityBillUrl
            )
            for ((label, url) in docFields) {
                if (!url.isNullOrEmpty()) {
                    val btn = Button(this).apply {
                        text = "View $label"
                        setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                        }
                    }
                    filesLayout.addView(btn)
                }
            }
            if (filesLayout.childCount == 0) {
                filesLayout.addView(TextView(this).apply { text = "No document files uploaded." })
            }

            // --- Images block ---
            imagesLayout.removeAllViews()
            if (!it.propertyImageUrls.isNullOrEmpty()) {
                for (imgUrl in it.propertyImageUrls) {
                    val img = ImageView(this)
                    img.layoutParams = LinearLayout.LayoutParams(350, 350).apply {
                        setMargins(0, 0, 24, 16)
                    }
                    img.scaleType = ImageView.ScaleType.CENTER_CROP
                    Glide.with(this).load(imgUrl).into(img)
                    img.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(imgUrl))
                        startActivity(intent)
                    }
                    imagesLayout.addView(img)
                }
            } else {
                imagesLayout.addView(TextView(this).apply { text = "No uploaded property images." })
            }
        }
    }
}
