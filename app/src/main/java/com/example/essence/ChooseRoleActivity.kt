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

        binding.btnBuyer.setOnClickListener {
            saveRoleAndContinue("buyer")
        }

        binding.btnSeller.setOnClickListener {
            saveRoleAndContinue("seller")
        }
    }

    private fun saveRoleAndContinue(role: String) {
        val userId = auth.currentUser?.uid ?: return
        val updates = mapOf("role" to role)

        db.collection("users").document(userId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Role saved: $role", Toast.LENGTH_SHORT).show()

                val intent = Intent(
                    this,
                    if (role == "buyer") BuyerMainActivity::class.java
                    else SellerHomeActivity::class.java
                )
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save role", Toast.LENGTH_SHORT).show()
            }
    }
}
