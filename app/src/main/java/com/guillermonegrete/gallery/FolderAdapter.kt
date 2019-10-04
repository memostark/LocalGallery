package com.guillermonegrete.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class FolderAdapter(private val folders: List<String>): RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.folder_item, parent, false)
        return ViewHolder(item)
    }

    override fun getItemCount() = folders.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = folders[position]
    }

    class ViewHolder(item: View): RecyclerView.ViewHolder(item){
        val name: Button = itemView.findViewById(R.id.name_text)
    }
}