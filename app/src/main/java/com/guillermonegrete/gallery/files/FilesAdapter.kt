package com.guillermonegrete.gallery.files

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.guillermonegrete.gallery.R

class FilesAdapter(private val files: List<String>): RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.file_item, parent, false)
        return ViewHolder(item)
    }

    override fun getItemCount() = files.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = files[position]
        holder.bind(item)
    }

    class ViewHolder(
        item: View
    ): RecyclerView.ViewHolder(item){

        private val name: TextView = itemView.findViewById(R.id.name_text)

        fun bind(item: String){
            name.text = item
        }
    }
}