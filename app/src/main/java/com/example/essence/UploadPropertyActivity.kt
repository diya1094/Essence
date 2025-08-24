package com.example.essence

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class UploadPropertyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_property)

        val yearOptions = listOf("1", "2", "3", "4", "5", "5+", "10+", "15+", "20+")
        val yearSpinner = findViewById<Spinner>(R.id.yearBuiltInput)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            yearOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = adapter

        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedYear = yearOptions[position]
                Toast.makeText(this@UploadPropertyActivity, "Selected: $selectedYear", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Property Type options
        val propertyTypes = listOf("Apartment/Flat", "Villa", "Condominiums", "Row House", "Penthouse", "Studio", "Bunglows", "Haveli", "Other")

        val propertyTypeSpinner: Spinner = findViewById(R.id.typeOfPropertyInput)

        val propertyAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            propertyTypes
        )
        propertyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        propertyTypeSpinner.adapter = propertyAdapter

        propertyTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedProperty = propertyTypes[position]
                Toast.makeText(this@UploadPropertyActivity, "Selected: $selectedProperty", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

    }
}