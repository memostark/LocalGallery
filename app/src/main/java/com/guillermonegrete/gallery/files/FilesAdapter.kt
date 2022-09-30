package com.guillermonegrete.gallery.files

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.ImageFile
import com.guillermonegrete.gallery.data.VideoFile
import com.guillermonegrete.gallery.databinding.FileVideoItemBinding

class FilesAdapter(
    private val viewModel: FilesViewModel
): PagingDataAdapter<File, FilesAdapter.ViewHolder>(FileDiffCallback) {

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

    open class ViewHolder(
        private val viewModel: FilesViewModel,
        item: View
    ): RecyclerView.ViewHolder(item){

        private val image: ImageView = itemView.findViewById(R.id.file_view)

        fun bind(item: File){
            itemView.layoutParams = FrameLayout.LayoutParams(item.width, item.height)
            val realPos = absoluteAdapterPosition
            image.setOnClickListener { viewModel.openFilesDetails(realPos) }

            Glide.with(itemView)
                .load(item.name)
                .placeholder(R.drawable.ic_image_24dp)
                .override(item.width, item.height)
                .centerCrop() // stretch the image to fit the view to avoid showing gaps
                .into(image)
        }
    }

    class VideoViewHolder(viewModel: FilesViewModel, private val binding: FileVideoItemBinding): ViewHolder(viewModel, binding.root){
        fun bind(item: VideoFile){
            super.bind(item)
            binding.duration.text = DateUtils.formatElapsedTime(item.duration.toLong())
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
