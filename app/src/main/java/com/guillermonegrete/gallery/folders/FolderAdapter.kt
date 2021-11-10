package com.guillermonegrete.gallery.folders

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.GetFolderResponse
import com.guillermonegrete.gallery.databinding.FolderItemBinding
import com.guillermonegrete.gallery.databinding.FolderNameItemBinding
import java.util.*

class FolderAdapter(
    private val viewModel: FoldersViewModel
): PagingDataAdapter<Folder, RecyclerView.ViewHolder>(FolderDiffCallback)/*, Filterable*/ {

//    private var filteredFolders = data.folders

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            R.layout.folder_name_item -> NameViewHolder(FolderNameItemBinding.inflate(inflater, parent, false))
            else -> ViewHolder(viewModel, FolderItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(position){
//            0 -> R.layout.folder_name_item
            else -> R.layout.folder_item
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        when(holder){
            is ViewHolder -> holder.bind(item)
            is NameViewHolder -> holder.bind("No title yet")
        }
    }

    /*override fun getFilter(): Filter {
        return object: Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                filteredFolders = if(constraint.isNullOrBlank()){
                    data.folders
                }else{
                    data.folders.filter { it.name.toLowerCase(Locale.getDefault()).contains(constraint) }
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
    }*/

    class ViewHolder(
        private val viewModel: FoldersViewModel,
        private val binding: FolderItemBinding,
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(item: Folder){
            with(binding) {
                Glide.with(itemView)
                    .load(item.coverUrl)
                    .into(coverImage)
                nameText.text = item.name
                itemsCountText.text = itemView.resources.getQuantityString(
                    R.plurals.folder_item_count_text,
                    item.count,
                    item.count
                )

                itemView.setOnClickListener { viewModel.openFolder(item.name) }
            }
        }
    }

    class NameViewHolder(private val binding: FolderNameItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(name: String){
             binding.rootFolder.text = name
        }
    }
}

object FolderDiffCallback : DiffUtil.ItemCallback<Folder>() {

    override fun areItemsTheSame(oldItem: Folder, newItem: Folder): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean {
        return oldItem == newItem
    }
}