package com.example.essence

import PropertySingleton
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class AdditionalDocsActivity : AppCompatActivity() {

    private val docTypes = listOf(
        "titleDeed", "nonDispute", "encumbrance", "propertyTax",
        "mutation", "possession", "noc", "utilityBill"
    )
    private val docTitles = listOf(
        "Proof of ownership (Title deed)",
        "No Dispute Affidavit",
        "Encumbrance Certificate",
        "Property tax receipt",
        "Mutation Document",
        "Possession Letter",
        "No Objection Certificate (NOC)",
        "Utility Bill Receipts"
    )

    private val selectedDocUris = mutableMapOf<String, Uri?>()
    private val pickerLaunchers = mutableMapOf<String, ActivityResultLauncher<String>>()
    private lateinit var submitBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_additional_docs)

        submitBtn = findViewById(R.id.submitBtn)
        submitBtn.setOnClickListener { storeUrisAndProceed() }
        submitBtn.isEnabled = false

        // Set up title for each included doc card and their pickers
        for (i in docTypes.indices) {
            val docType = docTypes[i]
            val docTitle = docTitles[i]
            val cardId = resources.getIdentifier("${docType}Card", "id", packageName)
            val card = findViewById<LinearLayout>(cardId)
            val titleView = card.findViewById<TextView>(R.id.docTitle)
            titleView.text = docTitle

            // Picker launcher for each docType (avoid loop issues)
            val pickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    selectedDocUris[docType] = uri
                    updateDocCards()
                    tryEnableSubmit()
                }
            }
            pickerLaunchers[docType] = pickerLauncher
            card.setOnClickListener { pickerLauncher.launch("*/*") }
        }
    }

    private fun updateDocCards() {
        for ((i, docType) in docTypes.withIndex()) {
            val cardId = resources.getIdentifier("${docType}Card", "id", packageName)
            val card = findViewById<LinearLayout>(cardId)
            val selected = selectedDocUris[docType] != null
            // Change background tint for selected state
            val bgRes = if (selected) R.drawable.rounded_box_selected else R.drawable.rounded_box
            card.setBackgroundResource(bgRes)
            // Optionally set docRequired text or color for selection too if desired
        }
    }

    private fun tryEnableSubmit() {
        submitBtn.isEnabled = docTypes.all { selectedDocUris[it] != null }
    }

    private fun storeUrisAndProceed() {
        // Put URIs for upload on payment screen
        PropertySingleton.titleDeedUri = selectedDocUris["titleDeed"]
        PropertySingleton.nonDisputeUri = selectedDocUris["nonDispute"]
        PropertySingleton.encumbranceUri = selectedDocUris["encumbrance"]
        PropertySingleton.propertyTaxUri = selectedDocUris["propertyTax"]
        PropertySingleton.mutationUri = selectedDocUris["mutation"]
        PropertySingleton.possessionUri = selectedDocUris["possession"]
        PropertySingleton.nocUri = selectedDocUris["noc"]
        PropertySingleton.utilityBillUri = selectedDocUris["utilityBill"]
        // Go to PaymentActivity (do upload/save after payment!)
        val intent = Intent(this, PaymentActivity::class.java)
        startActivity(intent)
    }
}
