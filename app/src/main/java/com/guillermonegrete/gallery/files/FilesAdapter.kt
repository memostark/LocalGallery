package com.guillermonegrete.gallery.files

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.ImageFile
import com.guillermonegrete.gallery.data.VideoFile
import com.guillermonegrete.gallery.databinding.FileVideoItemBinding
import io.reactivex.rxjava3.subjects.PublishSubject

class FilesAdapter(
    private val viewModel: FilesViewModel
): PagingDataAdapter<File, FilesAdapter.ViewHolder>(FileDiffCallback) {

    val onItemLongPress: PublishSubject<Int> = PublishSubject.create()

    private var multiSelect = false

    var selectedItems = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType){
            R.layout.file_video_item -> VideoViewHolder(viewModel, FileVideoItemBinding.inflate(inflater, parent, false))
            else -> ViewHolder(viewModel, inflater.inflate(viewType, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is ImageFile -> R.layout.file_image_item
            is VideoFile -> R.layout.file_video_item
            else -> R.layout.file_image_item
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        when(holder){
            is VideoViewHolder -> holder.bind(item as VideoFile)
            else -> holder.bind(item)
        }
    }

    open inner class ViewHolder(
        private val viewModel: FilesViewModel,
        item: View
    ): RecyclerView.ViewHolder(item){

        private val image: ImageView = itemView.findViewById(R.id.file_view)
        private val selectedIcon: ImageView = itemView.findViewById(R.id.selected_icon)

        fun bind(item: File){
            itemView.layoutParams = FrameLayout.LayoutParams(item.width, item.height)
            val realPos = absoluteAdapterPosition
            image.setOnClickListener { itemClicked(realPos) }

            image.setOnLongClickListener {
                onItemLongPress.onNext(realPos)
                selectedItems.add(realPos)
                true
            }

            if (multiSelect) {
                val imageSource = if(realPos in selectedItems) R.drawable.ic_twotone_check_circle_24 else R.drawable.ic_twotone_circle_24
                selectedIcon.setImageResource(imageSource)
            }
            selectedIcon.isVisible = multiSelect

            Glide.with(itemView)
                .load(item.name)
                .placeholder(R.drawable.ic_image_24dp)
                .override(item.width, item.height)
                .centerCrop() // stretch the image to fit the view to avoid showing gaps
                .into(image)
        }

        private fun itemClicked(position: Int) {
            if(multiSelect){
                if(position in selectedItems) {
                    selectedItems.remove(position)
                } else {
                    selectedItems.add(position)
                }
                notifyItemChanged(position)
            } else {
                viewModel.openFilesDetails(position)
            }
        }
    }

    inner class VideoViewHolder(viewModel: FilesViewModel, private val binding: FileVideoItemBinding): ViewHolder(viewModel, binding.root){
        fun bind(item: VideoFile){
            super.bind(item)
            binding.duration.text = DateUtils.formatElapsedTime(item.duration.toLong())
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectionMode(isMultiSelection: Boolean) {
        if(isMultiSelection != multiSelect) {
            multiSelect = isMultiSelection
            if(!multiSelect) selectedItems.clear()
            notifyDataSetChanged()
        }
    }
}

object FileDiffCallback : DiffUtil.ItemCallback<File>() {

    override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
        return oldItem == newItem
    }
}
