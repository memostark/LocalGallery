package com.guillermonegrete.gallery.files

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.flexbox.FlexboxLayoutManager
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.ImageFile
import com.guillermonegrete.gallery.data.VideoFile

class FilesAdapter(
    private val viewModel: FilesViewModel
): PagingDataAdapter<File, FilesAdapter.ViewHolder>(FileDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(viewModel, inflater.inflate(viewType, parent, false))
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
        holder.bind(item)
    }

    // TODO create View Holder for image and video item
    class ViewHolder(
        private val viewModel: FilesViewModel,
        item: View
    ): RecyclerView.ViewHolder(item){

        private val image: ImageView = itemView.findViewById(R.id.file_view)

        fun bind(item: File){
            itemView.layoutParams = FrameLayout.LayoutParams(item.width, item.height)
            val realPos = adapterPosition - 1
            image.setOnClickListener { viewModel.openFilesDetails(realPos) }

            Glide.with(itemView)
                .load(item.name)
                .placeholder(R.drawable.ic_image_24dp)
                .override(item.width, item.height)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(image)

            val params = image.layoutParams
            if (params is FlexboxLayoutManager.LayoutParams){
                params.flexGrow = 1.0f
            }
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
