package com.example.essence

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.essence.PropertyAdapter
import com.example.essence.databinding.ActivityAdminPropertyBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.jvm.java

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPropertyBinding
    private lateinit var adapter: PropertyAdapter
    private val db = FirebaseFirestore.getInstance()
    private val propertyList = mutableListOf<Property>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPropertyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PropertyAdapter(
            properties = propertyList,
            onApprove = { property -> approveProperty(property) },
            onReject = { property -> rejectProperty(property) },
            onRequestChange = { property -> requestChange(property) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadProperties()
    }

    private fun loadProperties() {
        db.collection("properties")
            .get()
            .addOnSuccessListener { result ->
                propertyList.clear()
                for (doc in result) {
                    val property = doc.toObject(Property::class.java)
                    property.propertyId = doc.id
                    propertyList.add(property)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun approveProperty(property: Property) {
        db.collection("properties").document(property.propertyId!!)
            .update("status", "active")
            .addOnSuccessListener {
                property.status = "active"
                adapter.notifyDataSetChanged()
            }
    }

    private fun rejectProperty(property: Property) {
        db.collection("properties").document(property.propertyId!!)
            .update("status", "rejected")
            .addOnSuccessListener {
                property.status = "rejected"
                adapter.notifyDataSetChanged()
            }
    }

    private fun requestChange(property: Property) {
        db.collection("properties").document(property.propertyId!!)
            .update("status", "change_required")
            .addOnSuccessListener {
                property.status = "change_required"
                adapter.notifyDataSetChanged()
            }
    }
}
