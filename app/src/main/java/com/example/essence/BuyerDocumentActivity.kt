package com.example.essence

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class BuyerDocumentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_document)

        val propertyId = intent.getParcelableExtra<Property>("property")?.propertyId
        if (propertyId == null) {
            Toast.makeText(this, "No property found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("properties")
            .document(propertyId)
            .get()
            .addOnSuccessListener { doc ->
                val property = doc.toObject(Property::class.java)
                if (property == null) {
                    Toast.makeText(this, "Property not found!", Toast.LENGTH_LONG).show()
                    finish()
                    return@addOnSuccessListener
                }
                if (!BuyerPaymentStatusManager.isPaymentDone(this, property.propertyId)) {
                    Toast.makeText(this, "Payment required to view documents", Toast.LENGTH_LONG).show()
                    finish()
                    return@addOnSuccessListener
                }

                val tvDocTitle: TextView = findViewById(R.id.tvDocTitle)
                tvDocTitle.text = "Documents for ${property.title}"

                val docsRecycler = findViewById<RecyclerView>(R.id.docsRecyclerView)
                docsRecycler.layoutManager = LinearLayoutManager(this)

                // Only use the actual document fields from your Property model
                val documents = listOf(
                    "Title Deed" to property.titleDeedUrl,
                    "Encumbrance" to property.encumbranceUrl,
                    "Mutation" to property.mutationUrl,
                    "Property Tax" to property.propertyTaxUrl,
                    "Possession" to property.possessionUrl,
                    "NOC" to property.nocUrl,
                    "Utility Bill" to property.utilityBillUrl
                ).filter { !it.second.isNullOrBlank() }
                    .map { it.first to it.second!! } // <-- make second element non-nullable

                docsRecycler.adapter = PropertyDocumentAdapter(documents)


                docsRecycler.adapter = PropertyDocumentAdapter(documents)

            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load property from server", Toast.LENGTH_LONG).show()
                finish()
            }
    }
}
