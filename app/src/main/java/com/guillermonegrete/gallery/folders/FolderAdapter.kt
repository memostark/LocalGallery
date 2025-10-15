package com.guillermonegrete.gallery.folders

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.color.MaterialColors
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.databinding.FolderItemBinding
import com.guillermonegrete.gallery.databinding.FolderNameItemBinding
import com.guillermonegrete.gallery.folders.models.FolderUI
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlin.math.abs


class FolderAdapter : PagingDataAdapter<FolderUI, RecyclerView.ViewHolder>(FolderDiffCallback) {

    val clickSubject: PublishSubject<ClickInfo> = PublishSubject.create()
    val longPressSubject: PublishSubject<Int> = PublishSubject.create()
    val itemSelectedSubject: PublishSubject<Int> = PublishSubject.create()

    var multiSelect = false
        private set

    val selectedItems = mutableSetOf<Int>()
    val selectedIds: MutableSet<Long>
        get() {
            return selectedItems.mapNotNull { (peek(it) as? FolderUI.Model)?.id }.toMutableSet()
        }

    var cardDefaultBackgroundColor = 0
    var cardDefaultTextColor = 0
    var selectedContainerColor = 0
    var selectedTextColor = 0

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val context = recyclerView.context
        val currentNightMode = recyclerView.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        cardDefaultBackgroundColor = ContextCompat.getColor(context, if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) R.color.cardview_dark_background else R.color.cardview_light_background)
        cardDefaultTextColor =  MaterialColors.getColor(context, R.attr.colorOnSurface, Color.BLACK)
        selectedContainerColor = MaterialColors.getColor(context, R.attr.colorPrimaryContainer, Color.WHITE)
        selectedTextColor =  MaterialColors.getColor(context, R.attr.colorOnPrimaryContainer, Color.BLACK)
    }

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

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectionMode(isMultiSelection: Boolean) {
        if(isMultiSelection != multiSelect) {
            multiSelect = isMultiSelection
            if(!multiSelect) {
                selectedItems.clear()
            }
            notifyDataSetChanged()
        }
    }

    fun setSelected(position: Int) {
        selectedItems.add(position)
        notifyItemChanged(position)
    }

    fun setSelected(start: Int, end: Int) {
        selectedItems.addAll(start..end)
        val count = abs(end - start) + 1 // add one because the end index is inclusive
        notifyItemRangeChanged(start, count)
    }

    fun setUnselected(start: Int, end: Int) {
        selectedItems.removeAll(start..end)
        val count = abs(end - start) + 1
        notifyItemRangeChanged(start, count)
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
                    val pos = absoluteAdapterPosition
                    if(multiSelect){
                        if(pos in selectedItems) {
                            selectedItems.remove(pos)
                        } else {
                            selectedItems.add(pos)
                        }
                        notifyItemChanged(pos)
                        itemSelectedSubject.onNext(pos)
                    } else {
                        clickSubject.onNext(ClickInfo(absoluteAdapterPosition, item))
                    }
                }

                root.setOnLongClickListener {
                    longPressSubject.onNext(absoluteAdapterPosition)
                    true
                }

                val isSelected = multiSelect && absoluteAdapterPosition in selectedItems
                val background = if (isSelected) selectedContainerColor else cardDefaultBackgroundColor
                val text = if (isSelected) selectedTextColor else cardDefaultTextColor
                root.setCardBackgroundColor(background)
                nameText.setTextColor(text)
                itemsCountText.setTextColor(text)
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