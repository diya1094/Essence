package com.example.essence

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PropertyImageAdapter(private val imageUrls: List<String>)
    : RecyclerView.Adapter<PropertyImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.itemImage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_property_image_adapter, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val url = imageUrls[position]
        Glide.with(holder.imageView.context)
            .load(url)
            .placeholder(R.drawable.ic_property_image)
            .error(R.drawable.ic_property_image)
            .centerCrop()
            .into(holder.imageView)

        holder.imageView.setOnClickListener {
            val context = holder.imageView.context
            val intent = Intent(context, FullScreenImageActivity::class.java)
            intent.putStringArrayListExtra("imageUrls", ArrayList(imageUrls))
            intent.putExtra("initialPosition", position)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = imageUrls.size
}
