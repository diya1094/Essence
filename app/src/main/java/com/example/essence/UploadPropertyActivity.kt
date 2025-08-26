package com.example.essence

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UploadPropertyActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
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

        // Initialize Views
        propertyDescription = findViewById(R.id.propertyDescriptionInput)
        propertyPrice = findViewById(R.id.listingPriceInput)
        propertyAddress = findViewById(R.id.addressInput)
        yearBuiltInput = findViewById(R.id.yearBuiltInput)
        typeOfPropertyInput = findViewById(R.id.typeOfPropertyInput)
        saveContinueBtn = findViewById(R.id.saveAndContinueBtn)

        // Year Built Spinner
        val yearOptions = listOf("less than a year","1", "2", "3", "4", "5", "5+", "10+", "15+", "20+")
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearOptions)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearBuiltInput.adapter = yearAdapter

        // Property Type Spinner
        val propertyTypes = listOf(
            "Apartment/Flat", "Villa", "Condominiums", "Row House",
            "Penthouse", "Studio", "Bungalows", "Haveli", "Other"
        )
        val propertyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, propertyTypes)
        propertyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeOfPropertyInput.adapter = propertyAdapter

        // Save and Continue
        saveContinueBtn.setOnClickListener {
            saveProperty()
        }
    }

    private fun saveProperty() {
        val description = propertyDescription.text.toString().trim()
        val price = propertyPrice.text.toString().trim()
        val address = propertyAddress.text.toString().trim()
        val yearBuilt = yearBuiltInput.selectedItem?.toString() ?: ""
        val propertyType = typeOfPropertyInput.selectedItem?.toString() ?: ""

        // Validation
        if (description.isEmpty() || price.isEmpty() || address.isEmpty() ||
            yearBuilt.isEmpty() || propertyType.isEmpty()
        ) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate document reference with ID
        val propertyRef = db.collection("properties").document()
        val propertyId = propertyRef.id

        val propertyData = hashMapOf<String, Any>(
            "id" to propertyId, // store Firestore doc id in data
            "description" to description,
            "price" to price,
            "address" to address,
            "yearBuilt" to yearBuilt,
            "propertyType" to propertyType,
            "status" to "pending",
            "userId" to (auth.currentUser?.uid ?: "")
        )

        savePropertyToFirestore(propertyRef.id, propertyData)
    }

    private fun savePropertyToFirestore(propertyId: String, propertyData: HashMap<String, Any>) {
        db.collection("properties")
            .document(propertyId)
            .set(propertyData) // instead of .add()
            .addOnSuccessListener {
                Toast.makeText(this, "Property submitted! Waiting for admin approval.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save property", Toast.LENGTH_SHORT).show()
            }
    }
}
