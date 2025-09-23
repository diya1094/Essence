package com.example.essence

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PropertyDocumentAdapter(
    private val items: List<Pair<String, String>>
) : RecyclerView.Adapter<PropertyDocumentAdapter.DocViewHolder>() {

    class DocViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvDocName)
        val btnView: Button = view.findViewById(R.id.btnViewDoc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_property_document_adapter, parent, false)
        return DocViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocViewHolder, position: Int) {
        val (name, url) = items[position]
        holder.tvName.text = name
        holder.btnView.text = "View"
        holder.btnView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount() = items.size
}
