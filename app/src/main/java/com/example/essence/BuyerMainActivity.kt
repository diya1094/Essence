package com.example.essence

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
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
    private val filteredProperties = mutableListOf<Property>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_main)

        recyclerView = findViewById(R.id.rvProperties)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = BuyerPropertyAdapter(filteredProperties) { property ->
            val intent = Intent(this, PropertyDetailActivity::class.java)
            intent.putExtra("property", property)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // --- SEARCH BAR LOGIC ---
        val searchBar = findViewById<EditText>(R.id.etSearch) // Matches your XML!
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterProperties(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_saved -> {
                    startActivity(Intent(this, SavedActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
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
                filterProperties(findViewById<EditText>(R.id.etSearch).text?.toString() ?: "")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterProperties(query: String) {
        val lower = query.trim().lowercase()
        filteredProperties.clear()

        if (lower.isEmpty()) {
            filteredProperties.addAll(propertyList)
        } else {
            // Detect and parse special queries
            var minPrice: Int? = null
            var maxPrice: Int? = null
            var priceEquals: Int? = null

            // Parse known advanced filter patterns
            val minPrefix = "min:"
            val maxPrefix = "max:"
            val pricePrefix = "price:"
            val sizePrefix = "sqft:"

            // Find min price
            Regex("""min:(\d+)""").find(lower)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let {
                minPrice = it
            }
            // Find max price
            Regex("""max:(\d+)""").find(lower)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let {
                maxPrice = it
            }
            // Find exact price
            Regex("""price:(\d+)""").find(lower)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let {
                priceEquals = it
            }
            // Find sqft
            var sqft: Int? = null
            Regex("""sqft:(\d+)""").find(lower)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let {
                sqft = it
            }

            // Remove these filter commands from the main string for "generic" search
            var fuzzy = lower
                .replace(Regex("""min:\d+"""), "")
                .replace(Regex("""max:\d+"""), "")
                .replace(Regex("""price:\d+"""), "")
                .replace(Regex("""sqft:\d+"""), "")
                .trim()

            filteredProperties.addAll(
                propertyList.filter { prop ->
                    // Get property values for matching
                    val title = prop.title?.lowercase().orEmpty()
                    val address = prop.address?.lowercase().orEmpty()
                    val type = prop.propertyType?.lowercase().orEmpty()
                    val desc = prop.description?.lowercase().orEmpty()
                    val price = prop.price?.filter { it.isDigit() }?.toIntOrNull() // Your price as Int
                    val sq = prop.propertySize?.filter { it.isDigit() }?.toIntOrNull() // Square feet as Int

                    // Price filters
                    val matchesMin = minPrice?.let { price != null && price >= it } ?: true
                    val matchesMax = maxPrice?.let { price != null && price <= it } ?: true
                    val matchesExact = priceEquals?.let { price != null && price == it } ?: true

                    // Sqft filter
                    val matchesSqft = sqft?.let { sq != null && sq == it } ?: true

                    // Fuzzy match (title/location/type/desc includes the fuzzy text)
                    val fuzzyMatch = fuzzy.isEmpty() || listOf(title, address, type, desc).any { it.contains(fuzzy) }

                    matchesMin && matchesMax && matchesExact && matchesSqft && fuzzyMatch
                }
            )
        }
        adapter.notifyDataSetChanged()
    }

}
