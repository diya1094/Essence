package com.example.essence

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class FullScreenImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val imageUrls = intent.getStringArrayListExtra("imageUrls") ?: arrayListOf()
        val initialPosition = intent.getIntExtra("initialPosition", 0)

        val viewPager: ViewPager2 = findViewById(R.id.fullScreenViewPager)
        val adapter = PropertyImageAdapter(imageUrls)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(initialPosition, false)
    }
}
