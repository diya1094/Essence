package com.example.essence

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UploadPropertyActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var propertyTitle: EditText
    private lateinit var propertyDescription: EditText
    private lateinit var propertyPrice: EditText
    private lateinit var propertyAddress: EditText
    private lateinit var yearBuiltInput: Spinner
    private lateinit var typeOfPropertyInput: Spinner
    private lateinit var saveContinueBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_property)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        propertyTitle = findViewById(R.id.title)
        propertyDescription = findViewById(R.id.propertyDescriptionInput)
        propertyPrice = findViewById(R.id.listingPriceInput)
        propertyAddress = findViewById(R.id.addressInput)
        yearBuiltInput = findViewById(R.id.yearBuiltInput)
        typeOfPropertyInput = findViewById(R.id.typeOfPropertyInput)
        saveContinueBtn = findViewById(R.id.saveAndContinueBtn)

        val yearOptions = listOf("less than a year", "1", "2", "3", "4", "5", "5+", "10+", "15+", "20+")
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearOptions)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearBuiltInput.adapter = yearAdapter

        val propertyTypes = listOf(
            "Apartment/Flat", "Villa", "Condominiums", "Row House",
            "Penthouse", "Studio", "Bungalows", "Haveli", "Other"
        )
        val propertyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, propertyTypes)
        propertyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeOfPropertyInput.adapter = propertyAdapter

        saveContinueBtn.setOnClickListener {
            if (!validateForm()) return@setOnClickListener

            val intent = Intent(this, PaymentActivity::class.java)
            paymentLauncher.launch(intent)
        }
    }
    private val paymentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK &&
            result.data?.getStringExtra("payment_status") == "success"
        ) {
            savePropertyToFirestore()
        } else {
            Toast.makeText(this, "Payment failed. Cannot list property", Toast.LENGTH_LONG).show()
        }
    }

    private fun validateForm(): Boolean {
        val title = propertyTitle.text.toString().trim()
        val description = propertyDescription.text.toString().trim()
        val price = propertyPrice.text.toString().trim()
        val address = propertyAddress.text.toString().trim()
        val yearBuilt = yearBuiltInput.selectedItem?.toString() ?: ""
        val propertyType = typeOfPropertyInput.selectedItem?.toString() ?: ""

        return if (title.isEmpty() || description.isEmpty() || price.isEmpty() || address.isEmpty() ||
            yearBuilt.isEmpty() || propertyType.isEmpty()
        ) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            false
        } else true
    }

    private fun savePropertyToFirestore() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(this, "Not logged in. Cannot save property.", Toast.LENGTH_SHORT).show()
            Log.w("FirestoreSave", "User not logged in.")
            return
        }

        val title = propertyTitle.text.toString().trim()
        val description = propertyDescription.text.toString().trim()
        val price = propertyPrice.text.toString().trim()
        val address = propertyAddress.text.toString().trim()
        val yearBuilt = yearBuiltInput.selectedItem?.toString() ?: ""
        val propertyType = typeOfPropertyInput.selectedItem?.toString() ?: ""

        val propertyRef = db.collection("properties").document()
        val propertyDocumentId = propertyRef.id

        val propertyData = hashMapOf<String, Any>(
            "propertyId" to propertyDocumentId,
            "title" to title,
            "description" to description,
            "price" to price,
            "address" to address,
            "yearBuilt" to yearBuilt,
            "propertyType" to propertyType,
            "status" to "pending",
            "userId" to currentUserId,
            "createdAt" to System.currentTimeMillis()
        )

        Log.d("FirestoreSave", "Attempting to save property data: $propertyData")

        propertyRef.set(propertyData)
            .addOnSuccessListener {
                Log.i("FirestoreSave", "Property data saved successfully for document ID: $propertyDocumentId")
                Toast.makeText(
                    this,
                    "Property submitted for approval.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreSave", "Failed to save property data for document ID: $propertyDocumentId", e)
                Toast.makeText(
                    this,
                    "Failed to save property details. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
