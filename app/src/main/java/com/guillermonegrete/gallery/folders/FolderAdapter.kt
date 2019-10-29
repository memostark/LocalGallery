package com.guillermonegrete.gallery.folders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.Folder

class FolderAdapter(
    private val folders: List<Folder>,
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
        private val name: TextView = itemView.findViewById(R.id.name_text)
        private val itemCount: TextView = itemView.findViewById(R.id.items_count_text)

        fun bind(item: Folder){
            name.text = item.name
            itemCount.text = itemView.resources.getQuantityString(R.plurals.folder_item_count_text, item.count, item.count)

            itemView.setOnClickListener {
                viewModel.openFolder(item.name)
            }
        }
    }
}