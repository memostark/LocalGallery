package com.guillermonegrete.gallery.files.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.databinding.TagSuggestionItemBinding

class TagSuggestionsAdapter(private val listener: (Tag) -> Unit): ListAdapter<Tag, TagSuggestionsAdapter.ViewHolder>(TagDiffCallback) {

    private var unfilteredSet = mutableSetOf<Tag>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(TagSuggestionItemBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(val binding: TagSuggestionItemBinding): RecyclerView.ViewHolder(binding.root){

        init {
            binding.root.setOnClickListener {
                listener(getItem(absoluteAdapterPosition))
            }
        }

        fun bind(tag: Tag){
            binding.tagNameText.text = tag.name
        }
    }

    fun remove(tag: Tag){
        unfilteredSet.remove(tag)
        submitList(unfilteredSet.toList())
    }

    fun add(tag: Tag){
        if (unfilteredSet.add(tag)) submitList(unfilteredSet.toList())
    }

    fun modifyList(list : Set<Tag>) {
        unfilteredSet = list.toMutableSet()
        submitList(list.toList())
    }

    fun removeItems(list : Set<Tag>) {
        unfilteredSet.removeAll(list)
        submitList(unfilteredSet.toList())
    }

    fun filter(query: CharSequence) {
        val list = mutableListOf<Tag>()

        if(query.isNotEmpty()) {
            list.addAll(unfilteredSet.filter {
                it.name.lowercase().contains(query.toString().lowercase())
            })
        } else {
            list.addAll(unfilteredSet)
        }

        submitList(list)
    }

    object TagDiffCallback : DiffUtil.ItemCallback<Tag>() {

        override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean {
            return oldItem == newItem
        }
    }
}
