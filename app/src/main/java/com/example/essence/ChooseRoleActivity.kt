package com.example.essence

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.essence.databinding.ActivityChooseRoleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ChooseRoleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChooseRoleBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseRoleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Not logged in. Redirecting...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 🔹 Step 1: First check SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val savedRole = sharedPref.getString("userRole", null)

        if (savedRole != null) {
            // Role already saved locally → skip Firestore and go directly
            navigateToRoleScreen(savedRole)
            finish()
            return
        }

        // 🔹 Step 2: If not found locally, check Firestore
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.contains("role")) {
                    val firestoreRole = document.getString("role")
                    if (firestoreRole != null) {
                        // Save in SharedPreferences for next time
                        val editor = sharedPref.edit()
                        editor.putString("userRole", firestoreRole)
                        editor.apply()

                        navigateToRoleScreen(firestoreRole)
                        finish()
                        return@addOnSuccessListener
                    }
                }
                // If no role in Firestore → show buttons
                setupRoleButtons()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error checking role", Toast.LENGTH_SHORT).show()
                setupRoleButtons()
            }
    }

    private fun setupRoleButtons() {
        binding.btnBuyer.setOnClickListener {
            saveRoleAndContinue("buyer")
        }

        binding.btnSeller.setOnClickListener {
            saveRoleAndContinue("seller")
        }
    }

    private fun saveRoleAndContinue(role: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not found. Please login again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 🔹 Save role in SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("userRole", role)
        editor.apply()

        // 🔹 Navigate immediately
        navigateToRoleScreen(role)

        // 🔹 Save role in Firestore
        val updates = mapOf("role" to role)
        db.collection("users").document(userId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Role saved: $role", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save role", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToRoleScreen(role: String) {
        val intent = when (role) {
            "buyer" -> Intent(this, BuyerMainActivity::class.java)
            "seller" -> Intent(this, SellerHomeActivity::class.java)
            else -> Intent(this, LoginActivity::class.java) // fallback
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
