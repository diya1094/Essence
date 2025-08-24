package com.example.essence

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SellerHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_home_exisiting)

        val btnAddNewProperty: Button = findViewById(R.id.btnAddNewProperty)

        btnAddNewProperty.setOnClickListener {
            // Create an Intent to start UploadActivity
            val intent = Intent(this, UploadPropertyActivity::class.java)
            startActivity(intent)
        }
    }
}