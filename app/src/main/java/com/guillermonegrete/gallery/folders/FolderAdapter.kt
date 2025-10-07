package com.guillermonegrete.gallery.folders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.databinding.FolderItemBinding
import com.guillermonegrete.gallery.databinding.FolderNameItemBinding
import com.guillermonegrete.gallery.folders.models.FolderUI
import io.reactivex.rxjava3.subjects.PublishSubject

class FolderAdapter : PagingDataAdapter<FolderUI, RecyclerView.ViewHolder>(FolderDiffCallback) {

    val clickSubject: PublishSubject<ClickInfo> = PublishSubject.create()
    val longPressSubject: PublishSubject<FolderUI.Model> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            R.layout.folder_name_item -> NameViewHolder(FolderNameItemBinding.inflate(inflater, parent, false))
            else -> ViewHolder(FolderItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is FolderUI.Model -> R.layout.folder_item
            is FolderUI.HeaderModel -> R.layout.folder_name_item
            null -> throw IllegalStateException("Unknown view")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        when(holder){
            is ViewHolder -> holder.bind(item as FolderUI.Model)
            is NameViewHolder -> holder.bind((item as FolderUI.HeaderModel).title)
        }
    }

    inner class ViewHolder(
        private val binding: FolderItemBinding,
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(item: FolderUI.Model){
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

                itemView.setOnClickListener {
                    clickSubject.onNext(ClickInfo(absoluteAdapterPosition, item))
                }
            }

            binding.root.setOnLongClickListener {
                longPressSubject.onNext(item)
                true
            }
        }
    }

    class NameViewHolder(private val binding: FolderNameItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(name: String){
             binding.rootFolder.text = name
        }
    }

    data class ClickInfo(val position: Int, val item: FolderUI.Model)
}

object FolderDiffCallback : DiffUtil.ItemCallback<FolderUI>() {

    override fun areItemsTheSame(oldItem: FolderUI, newItem: FolderUI): Boolean {
        val isModel = oldItem is FolderUI.Model && newItem is FolderUI.Model && oldItem.name == newItem.name
        val isHeader = oldItem is FolderUI.HeaderModel && newItem is FolderUI.HeaderModel && oldItem.title == newItem.title
        return isModel || isHeader
    }

    override fun areContentsTheSame(oldItem: FolderUI, newItem: FolderUI): Boolean {
        return oldItem == newItem
    }
}