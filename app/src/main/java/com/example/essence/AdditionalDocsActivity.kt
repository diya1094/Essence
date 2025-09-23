package com.example.essence

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class AdditionalDocsActivity : AppCompatActivity() {
    private val docTypes = listOf("titleDeed", "nonDispute", "encumbrance", "propertyTax", "mutation", "possession", "noc", "utilityBill")
    private val docTitles = listOf("Proof of ownership (Title deed)", "No Dispute Affidavit", "Encumbrance Certificate", "Property tax receipt", "Mutation Document", "Possession Letter", "No Objection Certificate (NOC)", "Utility Bill Receipts")
    private val selectedDocUris = mutableMapOf<String, Uri?>()
    private val pickerLaunchers = mutableMapOf<String, ActivityResultLauncher<String>>()
    private lateinit var submitBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_additional_docs)
        submitBtn = findViewById(R.id.submitBtn)
        submitBtn.setOnClickListener { storeUrisAndProceed() }
        submitBtn.isEnabled = false

        selectedDocUris["titleDeed"] = PropertySingleton.titleDeedUri
        selectedDocUris["nonDispute"] = PropertySingleton.nonDisputeUri
        selectedDocUris["encumbrance"] = PropertySingleton.encumbranceUri
        selectedDocUris["propertyTax"] = PropertySingleton.propertyTaxUri
        selectedDocUris["mutation"] = PropertySingleton.mutationUri
        selectedDocUris["possession"] = PropertySingleton.possessionUri
        selectedDocUris["noc"] = PropertySingleton.nocUri
        selectedDocUris["utilityBill"] = PropertySingleton.utilityBillUri

        for (i in docTypes.indices) {
            val docType = docTypes[i]
            val docTitle = docTitles[i]
            val cardId = resources.getIdentifier("${docType}Card", "id", packageName)
            val card = findViewById<LinearLayout>(cardId)
            setupDocCard(card, docType, docTitle)
            val pickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    selectedDocUris[docType] = uri
                    setupDocCard(card, docType, docTitle)
                    tryEnableSubmit()
                }
            }
            pickerLaunchers[docType] = pickerLauncher
        }
        tryEnableSubmit()
    }

    private fun setupDocCard(card: LinearLayout, docType: String, docTitle: String) {
        card.removeAllViews()
        card.orientation = LinearLayout.HORIZONTAL
        val icon = ImageView(card.context)
        icon.setImageResource(R.drawable.ic_file)
        icon.layoutParams = LinearLayout.LayoutParams(70, 70)
        icon.setPadding(6, 0, 16, 0)
        card.addView(icon)
        val existing = selectedDocUris[docType]
        if (existing != null) {
            val fileNameView = TextView(card.context)
            fileNameView.text = getFileName(existing)
            fileNameView.textSize = 15f
            val removeBtn = ImageButton(card.context)
            removeBtn.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            removeBtn.setBackgroundColor(0x00000000)
            removeBtn.setOnClickListener {
                selectedDocUris[docType] = null
                setupDocCard(card, docType, docTitle)
                tryEnableSubmit()
            }
            card.addView(fileNameView)
            card.addView(removeBtn)
            card.setOnClickListener(null)
        } else {
            val docLabel = TextView(card.context)
            docLabel.text = docTitle
            docLabel.textSize = 15f
            card.addView(docLabel)
            card.setOnClickListener { pickerLaunchers[docType]?.launch("application/pdf") }
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "selected.pdf"
        val cur = contentResolver.query(uri, null, null, null, null)
        if (cur != null && cur.moveToFirst()) {
            val idx = cur.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            name = if (idx >= 0) cur.getString(idx) else name
            cur.close()
        }
        return name
    }

    private fun tryEnableSubmit() {
        submitBtn.isEnabled = docTypes.all { selectedDocUris[it] != null }
    }

    private fun storeUrisAndProceed() {
        PropertySingleton.titleDeedUri = selectedDocUris["titleDeed"]
        PropertySingleton.nonDisputeUri = selectedDocUris["nonDispute"]
        PropertySingleton.encumbranceUri = selectedDocUris["encumbrance"]
        PropertySingleton.propertyTaxUri = selectedDocUris["propertyTax"]
        PropertySingleton.mutationUri = selectedDocUris["mutation"]
        PropertySingleton.possessionUri = selectedDocUris["possession"]
        PropertySingleton.nocUri = selectedDocUris["noc"]
        PropertySingleton.utilityBillUri = selectedDocUris["utilityBill"]

        // Only update property, then return to seller home if editing
        if (PropertySingleton.editPropertyId != null) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val docUpdate = mapOf(
                "title" to PropertySingleton.title,
                "description" to PropertySingleton.description,
                "price" to PropertySingleton.price,
                "address" to PropertySingleton.address,
                "propertySize" to PropertySingleton.propertySize,
                "yearBuilt" to PropertySingleton.yearBuilt,
                "propertyType" to PropertySingleton.propertyType,
                "jointOwners" to PropertySingleton.jointOwners,
                "propertyImageUrls" to PropertySingleton.imageUris.map { it.toString() },
                "proofOfIdUrl" to PropertySingleton.identityDocUri?.toString(),
                "proofOfAddressUrl" to PropertySingleton.addressDocUri?.toString(),
                "titleDeedUrl" to PropertySingleton.titleDeedUri?.toString(),
                "nonDisputeUrl" to PropertySingleton.nonDisputeUri?.toString(),
                "encumbranceUrl" to PropertySingleton.encumbranceUri?.toString(),
                "propertyTaxUrl" to PropertySingleton.propertyTaxUri?.toString(),
                "mutationUrl" to PropertySingleton.mutationUri?.toString(),
                "possessionUrl" to PropertySingleton.possessionUri?.toString(),
                "nocUrl" to PropertySingleton.nocUri?.toString(),
                "utilityBillUrl" to PropertySingleton.utilityBillUri?.toString(),
                "status" to "pending"
            )
            db.collection("properties").document(PropertySingleton.editPropertyId!!)
                .update(docUpdate)
                .addOnSuccessListener {
                    Toast.makeText(this, "Property update sent for admin reverification!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SellerHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Docs update failed!", Toast.LENGTH_LONG).show()
                }
        } else {
            val intent = Intent(this, PaymentActivity::class.java)
            startActivity(intent)
        }
    }
}
