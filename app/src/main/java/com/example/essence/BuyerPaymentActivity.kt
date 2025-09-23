package com.example.essence

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class BuyerPaymentActivity : AppCompatActivity() {
    private lateinit var property: Property

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_payment)

        // Get property object from intent
        property = intent.getParcelableExtra<Property>("property")
            ?: run {
                Toast.makeText(this, "Error: Property not found", Toast.LENGTH_LONG).show()
                finish()
                return
            }

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etCardNumber = findViewById<TextInputEditText>(R.id.etCardNumber)
        val etExpiry = findViewById<TextInputEditText>(R.id.etExpiry)
        val etCvv = findViewById<TextInputEditText>(R.id.etCvv)
        val etZip = findViewById<TextInputEditText>(R.id.etZip)
        val btnPay = findViewById<Button>(R.id.btnPay)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        btnPay.setOnClickListener {
            val name = etName.text?.trim().toString()
            val cardNumber = etCardNumber.text?.trim().toString()
            val expiry = etExpiry.text?.trim().toString()
            val cvv = etCvv.text?.trim().toString()
            val zip = etZip.text?.trim().toString()

            if (name.isEmpty()) {
                etName.error = "Enter cardholder name"; return@setOnClickListener
            }
            if (!cardNumber.matches(Regex("^\\d{12,19}\$"))) {
                etCardNumber.error = "Enter valid card number"; return@setOnClickListener
            }
            if (!expiry.matches(Regex("^(0[1-9]|1[0-2])\\/(\\d{2})\$"))) {
                etExpiry.error = "Expiry format MM/YY"; return@setOnClickListener
            }
            if (!cvv.matches(Regex("^\\d{3,4}\$"))) {
                etCvv.error = "CVV must be 3 or 4 digits"; return@setOnClickListener
            }
            if (zip.isEmpty() || !zip.matches(Regex("\\d{4,10}"))) {
                etZip.error = "Enter valid zip code"; return@setOnClickListener
            }

            // Simulate payment success:
            BuyerPaymentStatusManager.setPaymentDone(this, property.propertyId)
            Toast.makeText(this, "Payment successful!", Toast.LENGTH_LONG).show()

            val intent = Intent(this, BuyerDocumentActivity::class.java)
            intent.putExtra("property", property)
            startActivity(intent)
            setResult(Activity.RESULT_OK, Intent().putExtra("payment_status", "success"))
            finish()
        }

        btnCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
