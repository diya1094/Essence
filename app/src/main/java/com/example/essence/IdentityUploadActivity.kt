package com.example.essence

import PropertySingleton
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class IdentityUploadActivity : AppCompatActivity() {

    private var proofOfIdUri: Uri? = null
    private var proofOfAddressUri: Uri? = null

    private lateinit var continueToNextDocBtn: Button

    private val selectProofOfId = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            proofOfIdUri = uri
            Toast.makeText(this, "Selected Government ID", Toast.LENGTH_SHORT).show()
            tryEnableContinue()
        }
    }
    private val selectProofOfAddress = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            proofOfAddressUri = uri
            Toast.makeText(this, "Selected Address Proof", Toast.LENGTH_SHORT).show()
            tryEnableContinue()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity_upload) // your xml filename

        findViewById<LinearLayout>(R.id.govtIdCard).setOnClickListener { selectProofOfId.launch("*/*") }
        findViewById<LinearLayout>(R.id.addressCard).setOnClickListener { selectProofOfAddress.launch("*/*") }
        continueToNextDocBtn = findViewById(R.id.continueToPaymentBtn)
        continueToNextDocBtn.setOnClickListener { goToNextDocActivity() }
        tryEnableContinue()
    }

    private fun tryEnableContinue() {
        continueToNextDocBtn.isEnabled = (proofOfIdUri != null && proofOfAddressUri != null)
    }

    private fun goToNextDocActivity() {
        PropertySingleton.identityDocUri = proofOfIdUri
        PropertySingleton.addressDocUri = proofOfAddressUri

        val nextDocIntent = Intent(this, AdditionalDocsActivity::class.java)
        startActivity(nextDocIntent)
    }
}
