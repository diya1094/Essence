package com.example.essence

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class SavedActivity : AppCompatActivity() {
    private lateinit var rvSaved: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved)

        rvSaved = findViewById(R.id.rvSaved)
        rvSaved.layoutManager = LinearLayoutManager(this)

        fetchAllPropertiesFromFirebase()

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_saved
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, BuyerMainActivity::class.java))
                    true
                }
                R.id.nav_saved -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchAllPropertiesFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("properties").get()
            .addOnSuccessListener { result ->
                val allProperties = result.map { it.toObject(Property::class.java) }
                val savedProperties = SavedPropertiesManager.getSavedProperties(this, allProperties)
                rvSaved.adapter = BuyerSavedAdapter(savedProperties)
            }
            .addOnFailureListener { exception ->
                Log.e("SavedActivity", "Error fetching properties", exception)
                rvSaved.adapter = BuyerSavedAdapter(emptyList())
            }
    }
}
