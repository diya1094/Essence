package com.example.essence

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.essence.databinding.ActivityPaymentBinding
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity() {

    private var uploadInProgress = false
    private lateinit var binding: ActivityPaymentBinding
    private var amount = 499.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateAmountText()

        binding.btnPay.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val card = binding.etCardNumber.text.toString().trim()
            val expiry = binding.etExpiry.text.toString().trim()
            val cvv = binding.etCvv.text.toString().trim()
            val zip = binding.etZip.text.toString().trim()
            if (name.isEmpty() || card.isEmpty() || expiry.isEmpty() || cvv.isEmpty() || zip.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                // PAYMENT SUCCESS -- DO UPLOAD HERE!
                uploadAllToSupabaseAndFirebase()
            }
        }


        binding.btnCancel.setOnClickListener {
            Toast.makeText(this, "Transaction cancelled", Toast.LENGTH_LONG).show()

            val resultIntent = Intent()
            resultIntent.putExtra("payment_status", "failed")
            setResult(Activity.RESULT_CANCELED, resultIntent)
            finish()
        }
    }
    private fun uploadAllToSupabaseAndFirebase() {
        if (uploadInProgress) return
        uploadInProgress = true
        runOnUiThread { binding.btnPay.isEnabled = false }
        Toast.makeText(this, "Uploading property, please wait...", Toast.LENGTH_SHORT).show()
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                // Upload images
                val photoUrls = mutableListOf<String>()
                for (uri in PropertySingleton.imageUris) {
                    val bytes = contentResolver.openInputStream(uri)?.readBytes() ?: continue
                    val name = "property_${System.currentTimeMillis()}_${System.nanoTime()}.jpg"
                    val storage = SupabaseManager.supabase.storage.from("Essence")
                    storage.upload(name, bytes)
                    photoUrls.add(storage.publicUrl(name))
                }
                // Upload docs -- suspend function!
                suspend fun uploadDoc(uri: Uri?, prefix: String): String? {
                    if (uri == null) return null
                    val bytes = contentResolver.openInputStream(uri)?.readBytes() ?: return null
                    val name = "${prefix}_${System.currentTimeMillis()}.pdf"
                    val storage = SupabaseManager.supabase.storage.from("Essence")
                    storage.upload(name, bytes)
                    return storage.publicUrl(name)
                }
                val docLinks = mapOf(
                    "proofOfIdUrl" to uploadDoc(PropertySingleton.identityDocUri, "proof_of_id"),
                    "proofOfAddressUrl" to uploadDoc(PropertySingleton.addressDocUri, "proof_of_address"),
                    "titleDeedUrl" to uploadDoc(PropertySingleton.titleDeedUri, "title_deed"),
                    "nonDisputeUrl" to uploadDoc(PropertySingleton.nonDisputeUri, "non_dispute_affidavit"),
                    "encumbranceUrl" to uploadDoc(PropertySingleton.encumbranceUri, "encumbrance_certificate"),
                    "propertyTaxUrl" to uploadDoc(PropertySingleton.propertyTaxUri, "property_tax_receipt"),
                    "mutationUrl" to uploadDoc(PropertySingleton.mutationUri, "mutation_doc"),
                    "possessionUrl" to uploadDoc(PropertySingleton.possessionUri, "possession_letter"),
                    "nocUrl" to uploadDoc(PropertySingleton.nocUri, "noc"),
                    "utilityBillUrl" to uploadDoc(PropertySingleton.utilityBillUri, "utility_bill")
                )
                // Save to Firestore (include all info + file URLs!)
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val propertyId = db.collection("properties").document().id
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val userId = auth.currentUser?.uid ?: ""
                val propertyData = mapOf(
                    "propertyId" to propertyId,
                    "title" to PropertySingleton.title,
                    "description" to PropertySingleton.description,
                    "price" to PropertySingleton.price,
                    "address" to PropertySingleton.address,
                    "yearBuilt" to PropertySingleton.yearBuilt,
                    "propertyType" to PropertySingleton.propertyType,
                    "jointOwners" to PropertySingleton.jointOwners,
                    "propertyImageUrls" to photoUrls,
                    "status" to "pending",
                    "userId" to userId,
                    "createdAt" to System.currentTimeMillis()
                ) + docLinks
                db.collection("properties").document(propertyId).set(propertyData)
                    .addOnSuccessListener {
                        runOnUiThread {
                            Toast.makeText(this@PaymentActivity, "Property uploaded!", Toast.LENGTH_SHORT).show()
                            uploadInProgress = false
                            val intent = Intent(this@PaymentActivity, SellerHomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }.addOnFailureListener {
                        runOnUiThread {
                            Toast.makeText(this@PaymentActivity, "Failed to save property!", Toast.LENGTH_LONG).show()
                            binding.btnPay.isEnabled = true
                            uploadInProgress = false
                        }
                    }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@PaymentActivity, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                    binding.btnPay.isEnabled = true
                    uploadInProgress = false
                }
            }
        }
    }


    private fun updateAmountText() {
        binding.btnPay.text = "Pay $$amount"
    }
}
