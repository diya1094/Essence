package com.example.essence

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

    private lateinit var supabaseUploadLauncher: ActivityResultLauncher<String>
    private lateinit var uploadPhotoLayout: LinearLayout
    private val selectedFileUris = mutableListOf<Uri>()
    private val uploadedFileUrls = mutableListOf<String>()

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
        uploadPhotoLayout = findViewById(R.id.uploadPhotoLayout)

        val ownerCounts = (0..5).map { it.toString() }
        val ownerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ownerCounts)
        ownerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jointOwnerCountSpinner.adapter = ownerAdapter
        jointOwnerCountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val count = ownerCounts[position].toInt()
                jointOwnerContainer.removeAllViews()
                jointOwnerInputs.clear()
                for (i in 1..count) {
                    val ownerLayout = LinearLayout(this@UploadPropertyActivity)
                    ownerLayout.orientation = LinearLayout.VERTICAL
                    ownerLayout.setPadding(0, 16, 0, 16)

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

            // Store all property info in PropertySingleton
            PropertySingleton.title = propertyTitle.text.toString()
            PropertySingleton.description = propertyDescription.text.toString()
            PropertySingleton.price = propertyPrice.text.toString()
            PropertySingleton.address = propertyAddress.text.toString()
            PropertySingleton.yearBuilt = yearBuiltInput.selectedItem?.toString() ?: ""
            PropertySingleton.propertyType = typeOfPropertyInput.selectedItem?.toString() ?: ""
            PropertySingleton.jointOwners = jointOwnerInputs.map {
                mapOf(
                    "name" to it.first.text.toString(),
                    "email" to it.second.text.toString(),
                    "relation" to it.third.text.toString()
                )
            }
            PropertySingleton.imageUris = ArrayList(selectedFileUris)
            val intent = Intent(this, IdentityUploadActivity::class.java)
            startActivity(intent)
        }


        supabaseUploadLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedFileUris.add(uri)
                showSelectedImages()
            }
        }
        showSelectedImages()
    }
    private fun validateForm(): Boolean {
        val title = propertyTitle.text.toString().trim()
        val description = propertyDescription.text.toString().trim()
        val price = propertyPrice.text.toString().trim()
        val address = propertyAddress.text.toString().trim()
        val yearBuilt = yearBuiltInput.selectedItem?.toString() ?: ""
        val propertyType = typeOfPropertyInput.selectedItem?.toString() ?: ""
        return if (title.isEmpty() || description.isEmpty() || price.isEmpty() || address.isEmpty() || yearBuilt.isEmpty() || propertyType.isEmpty()
        ) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            false
        } else true
    }

    private fun showSelectedImages() {
        uploadPhotoLayout.removeAllViews()
        uploadPhotoLayout.orientation = LinearLayout.HORIZONTAL

        for ((index, uri) in selectedFileUris.withIndex()) {
            val frame = FrameLayout(this)
            val params = LinearLayout.LayoutParams(200, 200).apply {
                setMargins(8, 8, 8, 8)
            }
            frame.layoutParams = params

            val imageView = ImageView(this)
            imageView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageURI(uri)
            frame.addView(imageView)

            val removeBtn = ImageButton(this)
            val removeBtnSize = 48
            val removeParams = FrameLayout.LayoutParams(removeBtnSize, removeBtnSize, Gravity.END or Gravity.TOP)
            removeBtn.layoutParams = removeParams
            removeBtn.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            removeBtn.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
            removeBtn.setOnClickListener {
                selectedFileUris.removeAt(index)
                showSelectedImages()
            }
            frame.addView(removeBtn)
            uploadPhotoLayout.addView(frame)
        }

        val plusBtn = ImageButton(this)
        plusBtn.layoutParams = LinearLayout.LayoutParams(200, 200).apply {
            setMargins(8, 8, 8, 8)
        }
        plusBtn.setImageResource(android.R.drawable.ic_input_add)
        plusBtn.setBackgroundResource(R.drawable.rounded_box)
        plusBtn.setOnClickListener {
            supabaseUploadLauncher.launch("image/*")
        }
        uploadPhotoLayout.addView(plusBtn)
    }
}

