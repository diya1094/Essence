package com.example.essence

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
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
    private lateinit var jointOwnerCountSpinner: Spinner
    private lateinit var jointOwnerContainer: LinearLayout
    private val jointOwnerInputs = mutableListOf<Triple<EditText, EditText, EditText>>()

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

        jointOwnerCountSpinner = findViewById(R.id.jointOwnerCountSpinner)
        jointOwnerContainer = findViewById(R.id.jointOwnerContainer)

        val ownerCounts = (0..5).map { it.toString() }
        val ownerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ownerCounts)
        ownerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jointOwnerCountSpinner.adapter = ownerAdapter

        jointOwnerCountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val count = ownerCounts[position].toInt()
                jointOwnerContainer.removeAllViews()
                jointOwnerInputs.clear()

                for (i in 1..count) {
                    val ownerLayout = LinearLayout(this@UploadPropertyActivity)
                    ownerLayout.orientation = LinearLayout.VERTICAL
                    ownerLayout.setPadding(0, 16, 0, 16)

                    // Label + Input for Name
                    val nameLabel = TextView(this@UploadPropertyActivity).apply {
                        text = "Joint Owner $i Name"
                        setTextAppearance(android.R.style.TextAppearance_Medium)
                        setPadding(0, 8, 0, 8)
                    }
                    val nameInput = EditText(this@UploadPropertyActivity).apply {
                        hint = "Enter name"
                        setBackgroundResource(R.drawable.rounded_box)
                        setPadding(20, 20, 20, 20)
                    }

                    // Label + Input for Email
                    val emailLabel = TextView(this@UploadPropertyActivity).apply {
                        text = "Joint Owner $i Email"
                        setTextAppearance(android.R.style.TextAppearance_Medium)
                        setPadding(0, 8, 0, 8)
                    }
                    val emailInput = EditText(this@UploadPropertyActivity).apply {
                        hint = "Enter email"
                        inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        setBackgroundResource(R.drawable.rounded_box)
                        setPadding(20, 20, 20, 20)
                    }

                    // Label + Input for Relation
                    val relationLabel = TextView(this@UploadPropertyActivity).apply {
                        text = "Phone Number"
                        setTextAppearance(android.R.style.TextAppearance_Medium)
                        setPadding(0, 8, 0, 8)
                    }
                    val relationInput = EditText(this@UploadPropertyActivity).apply {
                        hint = "Enter Phone Number"
                        inputType = android.text.InputType.TYPE_CLASS_NUMBER
                        filters = arrayOf(android.text.InputFilter.LengthFilter(10))
                        setBackgroundResource(R.drawable.rounded_box)
                        setPadding(20, 20, 20, 20)
                    }

                    // Add views in order
                    ownerLayout.addView(nameLabel)
                    ownerLayout.addView(nameInput)
                    ownerLayout.addView(emailLabel)
                    ownerLayout.addView(emailInput)
                    ownerLayout.addView(relationLabel)
                    ownerLayout.addView(relationInput)

                    jointOwnerContainer.addView(ownerLayout)
                    jointOwnerInputs.add(Triple(nameInput, emailInput, relationInput))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

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

        val jointOwners = jointOwnerInputs.map {
            mapOf(
                "name" to it.first.text.toString().trim(),
                "email" to it.second.text.toString().trim(),
                "relation" to it.third.text.toString().trim()
            )
        }

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
            "createdAt" to System.currentTimeMillis(),
            "jointOwners" to jointOwners
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
