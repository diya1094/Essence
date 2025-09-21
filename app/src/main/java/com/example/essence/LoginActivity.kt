package com.example.essence

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.essence.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val db = FirebaseFirestore.getInstance()

    private val ADMIN_EMAIL = "admin@example.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInClient.signOut()

        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "User already logged in. Redirecting...")
            checkUserRoleAndRedirect()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Login"

        binding.btnLogin.setOnClickListener { loginUser() }
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.googleLoginButton.setOnClickListener { signInWithGoogle() }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
            return
        }
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Email/Password sign-in successful.")
                checkUserRoleAndRedirect()
            } else {
                Log.w(TAG, "Email/Password sign-in failed.", task.exception)
                Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(Exception::class.java)!!
                Log.d(TAG, "Google Account ID Token: ${account.idToken}")
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                mAuth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        Log.d(TAG, "Google sign-in successful with Firebase.")
                        checkIfUserExistsOrRedirect()
                    } else {
                        Log.w(TAG, "Firebase Google sign-in failed.", authTask.exception)
                        Toast.makeText(this, "Google Sign-In failed: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google sign-in getResult error: ${e.message}", e)
                Toast.makeText(this, "Google Sign-In error.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "Google sign-in failed or was cancelled. Result code: ${result.resultCode}")
        }
    }

    private fun checkIfUserExistsOrRedirect() {
        val userId = mAuth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "checkIfUserExistsOrRedirect: currentUser is null after Google Sign In!")
            Toast.makeText(this, "Authentication error. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d(TAG, "checkIfUserExistsOrRedirect: Checking for user $userId in Firestore 'users' collection.")

        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Log.i(TAG, "User $userId not found in 'users' collection. Redirecting to RegisterActivity.")
                    Toast.makeText(this, "New user. Please complete registration.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, RegisterActivity::class.java))
                    finish()
                } else {
                    Log.i(TAG, "User $userId found in 'users' collection. Proceeding to check role.")
                    checkUserRoleAndRedirect()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking user existence for $userId", e)
                Toast.makeText(this, "Error accessing user data. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUserRoleAndRedirect() {
        val currentUser = mAuth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "checkUserRoleAndRedirect: currentUser is null! Cannot proceed.")
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUser.email == ADMIN_EMAIL) {
            Log.d(TAG, "Admin email detected, redirecting to AdminActivity.")
            goToAdminActivity()
            return
        }

        val userId = currentUser.uid
        Log.d(TAG, "checkUserRoleAndRedirect: User ID: $userId")

        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    val role = userDoc.getString("role")
                    Log.d(TAG, "User role from Firestore: $role")

                    if (role.isNullOrEmpty()) {
                        Log.i(TAG, "User role is null or empty. Redirecting to ChooseRoleActivity.")
                        Toast.makeText(this, "Please choose your role.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, ChooseRoleActivity::class.java))
                        finish()
                    } else if (role.equals("buyer", ignoreCase = true)) {
                        Log.i(TAG, "User is a buyer. Redirecting to BuyerMainActivity.")
                        startActivity(Intent(this, BuyerMainActivity::class.java))
                        finish()
                    } else if (role.equals("seller", ignoreCase = true)) {
                        Log.i(TAG, "User is a seller. Checking for existing properties...")
                        val sellerIdFieldInProperties = "userId"

                        db.collection("properties")
                            .whereEqualTo(sellerIdFieldInProperties, userId)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { propertyQuerySnapshot ->
                                if (propertyQuerySnapshot.isEmpty) {
                                    Log.i(TAG, "Seller $userId has NO properties. Redirecting to SellerHomeActivity2 (New Seller Page).")
                                    startActivity(Intent(this, SellerHomeActivity2::class.java))
                                } else {
                                    Log.i(TAG, "Seller $userId HAS properties. Redirecting to SellerHomeActivity (Existing Seller Page).")
                                    startActivity(Intent(this, SellerHomeActivity::class.java))
                                }
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error querying properties for seller $userId", e)
                                Toast.makeText(this, "Error checking seller properties. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Log.w(TAG, "Unknown user role: $role. Defaulting to ChooseRoleActivity.")
                        Toast.makeText(this, "Unknown user role: $role", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, ChooseRoleActivity::class.java))
                        finish()
                    }
                } else {
                    Log.w(TAG, "User document for $userId does not exist. Redirecting to RegisterActivity.")
                    Toast.makeText(this, "User profile not found. Please register.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, RegisterActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching user role for $userId", e)
                Toast.makeText(this, "Error fetching user data. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToAdminActivity() {
        val intent = Intent(this, AdminActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
