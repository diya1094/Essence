package com.example.essence

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class BuyerMainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BuyerPropertyAdapter
    private val propertyList = mutableListOf<Property>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_main)

        recyclerView = findViewById(R.id.rvProperties)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = BuyerPropertyAdapter(propertyList) { property ->
            val intent = Intent(this, PropertyDetailActivity::class.java)
            intent.putExtra("property", property)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already in Home
                    true
                }
                R.id.nav_saved -> {
                    val profileIntent = Intent(this, SavedActivity::class.java)
                    startActivity(profileIntent)
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

        fetchApprovedProperties()
    }

    private fun fetchApprovedProperties() {
        val db = FirebaseFirestore.getInstance()
        db.collection("properties")
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { documents ->
                propertyList.clear()
                for (doc in documents) {
                    val property = doc.toObject(Property::class.java)
                    propertyList.add(property)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
