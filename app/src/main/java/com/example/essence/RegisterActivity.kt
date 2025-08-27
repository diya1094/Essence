package com.example.essence

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.essence.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Google Sign-In setup
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Google register button
        binding.googleRegisterButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        // Email/Password register button
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            user?.let {
                                val userId = it.uid
                                val userData = hashMapOf(
                                    "uid" to userId,
                                    "name" to (it.displayName ?: ""),
                                    "email" to it.email,
                                    "photoUrl" to (it.photoUrl?.toString() ?: ""),
                                    "createdAt" to System.currentTimeMillis()
                                )

                                val db = FirebaseFirestore.getInstance()
                                db.collection("users").document(userId).get()
                                    .addOnSuccessListener { document ->
                                        if (!document.exists()) {
                                            db.collection("users").document(userId).set(userData)
                                        }
                                    }

                                Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, ChooseRoleActivity::class.java))
                                finish()
                            }
                        } else {
                            Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Email and Password required", Toast.LENGTH_SHORT).show()
            }
        }

        // Already have account -> back to login
        binding.tvSignUp.setOnClickListener {
            finish()
        }
    }

    // Google Sign-In result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Authenticate with Google and save user data in Firestore
    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid
                        val name = it.displayName ?: ""
                        val email = it.email ?: ""
                        val photoUrl = it.photoUrl?.toString() ?: ""

                        val db = FirebaseFirestore.getInstance()

                        // ✅ Check if user already exists
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    // User already exists → don’t overwrite
                                    Toast.makeText(this, "Welcome back $name!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, ChooseRoleActivity::class.java))
                                    finish()
                                } else {
                                    // New user → create record
                                    val userData = hashMapOf(
                                        "uid" to userId,
                                        "name" to name,
                                        "email" to email,
                                        "photoUrl" to photoUrl,
                                        "createdAt" to System.currentTimeMillis()
                                    )

                                    db.collection("users").document(userId)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Signed in with Google & saved!", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, ChooseRoleActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Error saving user: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error checking user: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Firebase Auth failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
