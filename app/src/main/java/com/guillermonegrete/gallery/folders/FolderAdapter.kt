package com.guillermonegrete.gallery.folders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.Folder
import java.util.*

class FolderAdapter(
    private val folders: List<Folder>,
    private val viewModel: FoldersViewModel
): RecyclerView.Adapter<FolderAdapter.ViewHolder>(), Filterable {

    private var filteredFolders = folders

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.folder_item, parent, false)
        return ViewHolder(viewModel, item)
    }

    override fun getItemCount() = filteredFolders.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredFolders[position])
    }

    override fun getFilter(): Filter {
        return object: Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                filteredFolders = if(constraint.isNullOrBlank()){
                    folders
                }else{
                    folders.filter { it.name.toLowerCase(Locale.getDefault()).contains(constraint) }
                }

                val filterResults = FilterResults()
                filterResults.values = filteredFolders
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredFolders = results?.values as List<Folder>
                notifyDataSetChanged()
            }
        }
    }

    class ViewHolder(
        private val viewModel: FoldersViewModel,
        item: View
    ): RecyclerView.ViewHolder(item){
        private val cover: ImageView = itemView.findViewById(R.id.cover_image)
        private val name: TextView = itemView.findViewById(R.id.name_text)
        private val itemCount: TextView = itemView.findViewById(R.id.items_count_text)

        fun bind(item: Folder){
            Glide.with(itemView)
                .load(item.coverUrl)
                .into(cover)
            name.text = item.name
            itemCount.text = itemView.resources.getQuantityString(R.plurals.folder_item_count_text, item.count, item.count)

            itemView.setOnClickListener {
                viewModel.openFolder(item.name)
            }
        }
    }
}