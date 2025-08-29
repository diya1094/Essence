package com.example.essence

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.essence.databinding.ActivityPaymentBinding

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var amount = 499.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateAmountText()

        // ✅ Handle Pay Button
        binding.btnPay.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val card = binding.etCardNumber.text.toString().trim()
            val expiry = binding.etExpiry.text.toString().trim()
            val cvv = binding.etCvv.text.toString().trim()
            val zip = binding.etZip.text.toString().trim()

            if (name.isEmpty() || card.isEmpty() || expiry.isEmpty() || cvv.isEmpty() || zip.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Payment of $$amount successful", Toast.LENGTH_LONG).show()

                // ✅ Explicit result intent
                val resultIntent = Intent()
                resultIntent.putExtra("payment_status", "success")
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }

        // ✅ Handle Cancel Button
        binding.btnCancel.setOnClickListener {
            Toast.makeText(this, "Transaction cancelled", Toast.LENGTH_LONG).show()

            val resultIntent = Intent()
            resultIntent.putExtra("payment_status", "failed")
            setResult(Activity.RESULT_CANCELED, resultIntent)
            finish()
        }
    }

    private fun updateAmountText() {
        binding.btnPay.text = "Pay $$amount"
    }
}
