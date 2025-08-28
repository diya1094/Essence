package com.example.essence

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

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

    private lateinit var paymentsClient: PaymentsClient

    private val LISTING_FEE_AMOUNT = "99.00"
    private val CURRENCY_CODE = "INR"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_property)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        paymentsClient = Wallet.getPaymentsClient(
            this,
            Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .build()
        )

        propertyTitle = findViewById(R.id.title) // ADDED - Make sure R.id.title is correct
        propertyDescription = findViewById(R.id.propertyDescriptionInput)
        propertyPrice = findViewById(R.id.listingPriceInput)
        propertyAddress = findViewById(R.id.addressInput)
        yearBuiltInput = findViewById(R.id.yearBuiltInput)
        typeOfPropertyInput = findViewById(R.id.typeOfPropertyInput)
        saveContinueBtn = findViewById(R.id.saveAndContinueBtn)

        val yearOptions =
            listOf("less than a year", "1", "2", "3", "4", "5", "5+", "10+", "15+", "20+")
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearOptions)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearBuiltInput.adapter = yearAdapter

        val propertyTypes = listOf(
            "Apartment/Flat", "Villa", "Condominiums", "Row House",
            "Penthouse", "Studio", "Bungalows", "Haveli", "Other"
        )
        val propertyAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, propertyTypes)
        propertyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeOfPropertyInput.adapter = propertyAdapter

        saveContinueBtn.setOnClickListener {
            if (!validateForm()) return@setOnClickListener
            maybeShowGooglePay()
        }
    }

    private fun validateForm(): Boolean {
        val title = propertyTitle.text.toString().trim() // GET TITLE
        val description = propertyDescription.text.toString().trim()
        val price = propertyPrice.text.toString().trim()
        val address = propertyAddress.text.toString().trim()
        val yearBuilt = yearBuiltInput.selectedItem?.toString() ?: ""
        val propertyType = typeOfPropertyInput.selectedItem?.toString() ?: ""

        return if (title.isEmpty() || // VALIDATE TITLE
            description.isEmpty() || price.isEmpty() || address.isEmpty() ||
            yearBuilt.isEmpty() || propertyType.isEmpty()
        ) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            false
        } else true
    }

    private fun maybeShowGooglePay() {
        val isReadyJson = IsReadyToPayRequest.fromJson(GPayJson.isReadyToPayRequest().toString())
        val readyTask: Task<Boolean> = paymentsClient.isReadyToPay(isReadyJson)
        readyTask.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result == true) {
                requestPayment()
            } else {
                Toast.makeText(
                    this,
                    "Google Pay not available on this device/account.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun requestPayment() {
        val request = PaymentDataRequest.fromJson(
            GPayJson.paymentDataRequest(
                price = LISTING_FEE_AMOUNT,
                currency = CURRENCY_CODE
            ).toString()
        )
        val task = paymentsClient.loadPaymentData(request)
        AutoResolveHelper.resolveTask(task, this, LOAD_PAYMENT_DATA_REQUEST_CODE)
    }

    @Deprecated("onActivityResult is deprecated but used in this Google Pay sample logic")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val paymentData = data?.let { PaymentData.getFromIntent(it) }
                    handlePaymentSuccess(paymentData)
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show()
                }
                AutoResolveHelper.RESULT_ERROR -> {
                    val status = AutoResolveHelper.getStatusFromIntent(data)
                    Log.e("GooglePay", "Payment Error: ${status?.statusCode} - ${status?.statusMessage}")
                    Toast.makeText(this, "Payment failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Other results, log or ignore
                }
            }
        }
    }

    private fun handlePaymentSuccess(paymentData: PaymentData?) {
        try {
            val jsonString = paymentData?.toJson() ?: run {
                Log.e("GooglePay", "PaymentData is null in handlePaymentSuccess")
                Toast.makeText(this, "Payment data error.", Toast.LENGTH_SHORT).show()
                return
            }
            Log.d("GooglePay", "PaymentData JSON: $jsonString")
            val paymentMethodData = JSONObject(jsonString).getJSONObject("paymentMethodData")
            val tokenizationData = paymentMethodData.getJSONObject("tokenizationData")
            val token = tokenizationData.getString("token")
            Log.d("GooglePay", "Payment token: $token (In TEST mode, this is often 'examplePaymentMethodToken')")

            // In a real app, send this token to your backend for processing.
            // For this example, we assume success in TEST mode and save to Firestore.
            savePropertyToFirestore()

        } catch (e: JSONException) {
            Log.e("GooglePay", "handlePaymentSuccess JSONException: ", e)
            Toast.makeText(this, "Payment parsing error.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("GooglePay", "handlePaymentSuccess Exception: ", e)
            Toast.makeText(this, "An unexpected error occurred during payment processing.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePropertyToFirestore() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(this, "Not logged in. Cannot save property.", Toast.LENGTH_SHORT).show()
            Log.w("FirestoreSave", "User not logged in.")
            return
        }

        val title = propertyTitle.text.toString().trim() // GET TITLE
        val description = propertyDescription.text.toString().trim()
        val price = propertyPrice.text.toString().trim()
        val address = propertyAddress.text.toString().trim()
        val yearBuilt = yearBuiltInput.selectedItem?.toString() ?: ""
        val propertyType = typeOfPropertyInput.selectedItem?.toString() ?: ""

        val propertyRef = db.collection("properties").document()
        val propertyDocumentId = propertyRef.id // Use this as the document's ID

        val propertyData = hashMapOf<String, Any>(
            "propertyId" to propertyDocumentId, // Storing the document ID also as a field
            "title" to title,                   // ADDED TITLE TO DATA
            "description" to description,
            "price" to price,
            "address" to address,
            "yearBuilt" to yearBuilt,
            "propertyType" to propertyType,
            "status" to "pending",              // Initial status
            "userId" to currentUserId,          // Seller's Firebase UID
            "listingFeePaid" to true,           // Assuming payment was successful
            "listingFeeAmount" to LISTING_FEE_AMOUNT,
            "currency" to CURRENCY_CODE,
            "paidAt" to System.currentTimeMillis(),
            "createdAt" to System.currentTimeMillis() // General creation timestamp
            // Consider adding imageUrls list here if you implement image uploads
            // "imageUrls" to listOf<String>()
        )

        Log.d("FirestoreSave", "Attempting to save property data: $propertyData")

        propertyRef.set(propertyData) // Use the generated ref to set data with specific ID
            .addOnSuccessListener {
                Log.i("FirestoreSave", "Property data saved successfully for document ID: $propertyDocumentId")
                Toast.makeText(
                    this,
                    "Payment successful. Property submitted for approval.",
                    Toast.LENGTH_LONG
                ).show()
                finish() // Close the activity
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreSave", "Failed to save property data for document ID: $propertyDocumentId", e)
                Toast.makeText(this, "Payment processed, but failed to save property details. Please contact support.", Toast.LENGTH_LONG).show()
                // Potentially, you might want to try and reverse the payment or flag this for manual review
            }
    }

    companion object {
        private const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
    }
}

// GPayJson object remains the same
private object GPayJson {

    private fun allowedCardNetworks(): JSONArray =
        JSONArray(listOf("AMEX", "DISCOVER", "JCB", "MASTERCARD", "VISA"))

    private fun allowedCardAuthMethods(): JSONArray =
        JSONArray(listOf("PAN_ONLY", "CRYPTOGRAM_3DS"))

    private fun baseCardPaymentMethod(): JSONObject =
        JSONObject().apply {
            put("type", "CARD")
            put(
                "parameters", JSONObject().apply {
                    put("allowedAuthMethods", allowedCardAuthMethods())
                    put("allowedCardNetworks", allowedCardNetworks())
                    put("billingAddressRequired", true)
                    put(
                        "billingAddressParameters", JSONObject().apply {
                            put("format", "FULL")
                        }
                    )
                }
            )
        }

    private fun gatewayTokenizationSpecification(): JSONObject =
        JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put(
                "parameters", JSONObject().apply {
                    put("gateway", "example")
                    put("gatewayMerchantId", "exampleGatewayMerchantId")
                }
            )
        }

    private fun cardPaymentMethod(): JSONObject =
        baseCardPaymentMethod().apply {
            put("tokenizationSpecification", gatewayTokenizationSpecification())
        }

    fun isReadyToPayRequest(): JSONObject =
        JSONObject().apply {
            put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))
        }

    fun paymentDataRequest(price: String, currency: String): JSONObject =
        JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod()))
            put(
                "transactionInfo",
                JSONObject().apply {
                    put("totalPriceStatus", "FINAL")
                    put("totalPrice", ensurePriceFormat(price))
                    put("currencyCode", currency)
                }
            )
            put(
                "merchantInfo",
                JSONObject().apply {
                    put("merchantName", "Essence (TEST)") // Your app or company name
                }
            )
        }

    private fun ensurePriceFormat(value: String): String {
        return try {
            BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toPlainString()
        } catch (e: NumberFormatException) {
            Log.e("GPayJson", "Error formatting price: $value", e)
            "0.00" // Fallback to a default valid format
        } catch (e: Exception) {
            Log.e("GPayJson", "Unexpected error formatting price: $value", e)
            "0.00"
        }
    }
}
