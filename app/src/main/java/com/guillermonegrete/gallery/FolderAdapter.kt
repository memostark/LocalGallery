package com.guillermonegrete.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class FolderAdapter(
    private val folders: List<String>,
    private val viewModel: FoldersViewModel
): RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.folder_item, parent, false)
        return ViewHolder(viewModel, item)
    }

    override fun getItemCount() = folders.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(folders[position])

    }

    class ViewHolder(
        private val viewModel: FoldersViewModel,
        item: View
    ): RecyclerView.ViewHolder(item){
        private val name: Button = itemView.findViewById(R.id.name_text)

        fun bind(item: String){
            name.text = item
            name.setOnClickListener {
                viewModel.openFolder(item)
            }
        }
    }
}