package com.example.essence

import android.content.Intent
import android.os.Bundle
// import android.widget.Toast // If you want to use Toasts for unimplemented items
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView // Import

class BuyerMainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_main)

        bottomNavigationView = findViewById(R.id.bottomNavigation)

        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_search -> {
                    true
                }
                R.id.nav_saved-> {
                    true
                }
                R.id.nav_profile -> {
                    val profileIntent = Intent(this, ProfileActivity::class.java)
                    startActivity(profileIntent)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure the correct item is selected when returning to this activity
        if (::bottomNavigationView.isInitialized) {
            bottomNavigationView.selectedItemId = R.id.nav_home // Or your default selected item
        }
    }
}

