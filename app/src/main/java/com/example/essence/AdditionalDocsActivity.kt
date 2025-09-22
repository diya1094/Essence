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

        // Setup pickers & UI for each doc
        for (i in docTypes.indices) {
            val docType = docTypes[i]
            val docTitle = docTitles[i]
            val cardId = resources.getIdentifier("${docType}Card", "id", packageName)
            val card = findViewById<LinearLayout>(cardId)
            setupDocCard(card, docType, docTitle)
            val pickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    selectedDocUris[docType] = uri
                    setupDocCard(card, docType, docTitle) // Refresh
                    tryEnableSubmit()
                }
            }
            pickerLaunchers[docType] = pickerLauncher
        }
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
            // Disable card click, only "X" works for removal
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
        val intent = Intent(this, PaymentActivity::class.java)
        startActivity(intent)
    }
}
