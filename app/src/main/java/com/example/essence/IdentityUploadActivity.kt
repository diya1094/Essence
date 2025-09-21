package com.example.essence

import com.example.essence.PropertySingleton
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class IdentityUploadActivity : AppCompatActivity() {

    private var proofOfIdUri: Uri? = null
    private var proofOfAddressUri: Uri? = null

    private lateinit var continueToNextDocBtn: Button
    private lateinit var idLayout: LinearLayout
    private lateinit var addressLayout: LinearLayout

    private val selectProofOfId = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && isPdf(uri) && isFileSizeOk(uri)) {
            proofOfIdUri = uri
        } else if (uri != null) {
            Toast.makeText(this, "Select a PDF under 50MB", Toast.LENGTH_LONG).show()
        }
        updateDocBoxes()
        tryEnableContinue()
    }
    private val selectProofOfAddress = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && isPdf(uri) && isFileSizeOk(uri)) {
            proofOfAddressUri = uri
        } else if (uri != null) {
            Toast.makeText(this, "Select a PDF under 50MB", Toast.LENGTH_LONG).show()
        }
        updateDocBoxes()
        tryEnableContinue()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity_upload)

        idLayout = findViewById(R.id.govtIdCard)
        addressLayout = findViewById(R.id.addressCard)
        continueToNextDocBtn = findViewById(R.id.continueToPaymentBtn)

        idLayout.setOnClickListener { if (proofOfIdUri == null) selectProofOfId.launch("application/pdf") }
        addressLayout.setOnClickListener { if (proofOfAddressUri == null) selectProofOfAddress.launch("application/pdf") }
        continueToNextDocBtn.setOnClickListener { goToNextDocActivity() }
        tryEnableContinue()
        updateDocBoxes()
    }

    private fun tryEnableContinue() {
        continueToNextDocBtn.isEnabled = (proofOfIdUri != null && proofOfAddressUri != null)
    }

    private fun goToNextDocActivity() {
        PropertySingleton.identityDocUri = proofOfIdUri
        PropertySingleton.addressDocUri = proofOfAddressUri
        val nextDocIntent = Intent(this, AdditionalDocsActivity::class.java)
        startActivity(nextDocIntent)
        finish()

    }

    private fun updateDocBoxes() {
        showDocBox(
            idLayout,
            proofOfIdUri,
            "Upload Government ID (PDF)",
            onRemove = {
                proofOfIdUri = null
                updateDocBoxes()
                tryEnableContinue()
            }
        )
        showDocBox(
            addressLayout,
            proofOfAddressUri,
            "Upload Proof of Address (PDF)",
            onRemove = {
                proofOfAddressUri = null
                updateDocBoxes()
                tryEnableContinue()
            }
        )
    }

    private fun showDocBox(card: LinearLayout, uri: Uri?, label: String, onRemove: () -> Unit) {
        card.removeAllViews()
        val pad = 10
        card.setPadding(pad, pad, pad, pad)

        val context = card.context
        val icon = ImageView(context)
        icon.setImageResource(R.drawable.ic_file)
        icon.layoutParams = LinearLayout.LayoutParams(64, 64)
        icon.setPadding(6, 0, 16, 0)

        card.orientation = LinearLayout.HORIZONTAL
        card.addView(icon)

        if (uri != null) {
            val fileText = TextView(context)
            fileText.text = getFileName(uri)
            fileText.textSize = 14f

            val removeBtn = ImageButton(context)
            removeBtn.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            removeBtn.setBackgroundColor(0x00000000)
            removeBtn.setOnClickListener { onRemove() }

            card.addView(fileText)
            card.addView(removeBtn)
        } else {
            val docLabel = TextView(context)
            docLabel.text = label
            docLabel.textSize = 15f
            card.addView(docLabel)
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

    private fun isPdf(uri: Uri): Boolean =
        contentResolver.getType(uri) == "application/pdf"

    private fun isFileSizeOk(uri: Uri): Boolean {
        val fd = contentResolver.query(uri, null, null, null, null)
        val size = if (fd != null && fd.moveToFirst()) {
            val idx = fd.getColumnIndex(OpenableColumns.SIZE)
            val size = fd.getLong(idx)
            fd.close()
            size
        } else 0
        return size > 0 && size <= 50 * 1024 * 1024
    }
}
