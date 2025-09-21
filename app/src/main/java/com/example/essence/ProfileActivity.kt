package com.example.essence

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {
    private val TAG = "ProfileActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvUserName: TextView
    private lateinit var btnLogout: Button
    private lateinit var bottomNavigationView: BottomNavigationView
    private val SELLER_ID_FIELD_IN_PROPERTIES = "userId"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_user)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        tvRole = findViewById(R.id.tvRole)
        tvUserName = findViewById(R.id.tvUserName)
        btnLogout = findViewById(R.id.btnLogout)
        bottomNavigationView = findViewById(R.id.bottomNavigation)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "User not logged in. Redirecting to LoginActivity.")
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        Log.d(TAG, "Current User UID: ${currentUser.uid}")

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d(TAG, "User document found: ${document.data}")
                    val name = document.getString("name") ?: "N/A"
                    val email = document.getString("email") ?: currentUser.email ?: "N/A"
                    val role = document.getString("role") ?: ""
                    val username = document.getString("username") ?: "N/A"

                    tvName.text = name
                    tvEmail.text = email
                    tvRole.text = if (role.isEmpty()) "Unknown Role" else role.replaceFirstChar { it.titlecase() }
                    tvUserName.text = username

                    if (role.isEmpty()) {
                        Log.i(TAG, "Role is empty. Redirecting to ChooseRoleActivity.")
                        startActivity(Intent(this, ChooseRoleActivity::class.java))
                        finish()
                    } else {
                        if (intent.getBooleanExtra("fromLogin", false)) {
                            Log.d(TAG, "Navigating from login, calling redirectToHome.")
                            redirectToHome(role, currentUser.uid)
                        } else {
                            Log.d(TAG, "Setting up bottom navigation for role: $role")
                            setupBottomNavigation(role, currentUser.uid)
                        }
                    }
                } else {
                    Log.w(TAG, "User document for UID ${currentUser.uid} does not exist. Redirecting to ChooseRoleActivity.")
                    startActivity(Intent(this, ChooseRoleActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading profile document for UID ${currentUser.uid}", e)
                Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                tvName.text = "Error"
                tvEmail.text = "N/A"
                tvRole.text = "N/A"
                tvUserName.text = "N/A"
                setupBottomNavigation("buyer", currentUser.uid)
            }

        btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        Log.i(TAG, "Logging out user.")
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finishAffinity()
        }
    }

    private fun redirectToHome(role: String, userId: String) {
        if (role.equals("seller", ignoreCase = true)) {
            Log.d(TAG, "redirectToHome for seller: $userId. Checking properties with field '$SELLER_ID_FIELD_IN_PROPERTIES'.")
            firestore.collection("properties")
                .whereEqualTo(SELLER_ID_FIELD_IN_PROPERTIES, userId)
                .limit(1)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        Log.i(TAG, "Seller $userId has NO properties. Redirecting to SellerHomeActivity2 from login.")
                        startActivity(Intent(this, SellerHomeActivity2::class.java))
                    } else {
                        Log.i(TAG, "Seller $userId HAS properties. Redirecting to SellerHomeActivity from login.")
                        startActivity(Intent(this, SellerHomeActivity::class.java))
                    }
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error checking properties in redirectToHome for seller $userId", e)
                    Toast.makeText(this, "Error determining seller home. Defaulting.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SellerHomeActivity::class.java))
                    finish()
                }
        } else if (role.equals("buyer", ignoreCase = true)) {
            Log.i(TAG, "Redirecting buyer $userId to BuyerMainActivity from login.")
            startActivity(Intent(this, BuyerMainActivity::class.java))
            finish()
        } else {
            Log.w(TAG, "Unknown role '$role' in redirectToHome. Defaulting to ChooseRoleActivity.")
            startActivity(Intent(this, ChooseRoleActivity::class.java))
            finish()
        }
    }

    private fun setupBottomNavigation(role: String, userId: String) {
        bottomNavigationView.menu.clear()
        if (role.equals("seller", ignoreCase = true)) {
            Log.d(TAG, "Inflating seller_bottom_nav_menu.")
            bottomNavigationView.inflateMenu(R.menu.seller_bottom_nav_menu)
        } else {
            Log.d(TAG, "Inflating buyer_bottom_nav_menu.")
            bottomNavigationView.inflateMenu(R.menu.buyer_bottom_nav_menu)
        }

        bottomNavigationView.selectedItemId = R.id.nav_profile

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (role.equals("seller", ignoreCase = true)) {
                        firestore.collection("properties")
                            .whereEqualTo(SELLER_ID_FIELD_IN_PROPERTIES, userId)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                val targetClass = if (querySnapshot.isEmpty) {
                                    SellerHomeActivity2::class.java
                                } else {
                                    SellerHomeActivity::class.java
                                }
                                val intent = Intent(this, targetClass)
                                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                            }
                            .addOnFailureListener {
                                val intent = Intent(this, SellerHomeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                            }
                    } else {
                        val intent = Intent(this, BuyerMainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    }
                    true
                }
                R.id.nav_list -> {
                    val intent = Intent(this, SellerListingActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_saved -> {
                    if (role.equals("buyer", ignoreCase = true)) {
                        val intent = Intent(this, SavedActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.d(TAG, "Profile tab clicked. Already here for seller.")
                    }
                    true
                }
                else -> false
            }
        }
    }
}
