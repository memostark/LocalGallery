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
import com.guillermonegrete.gallery.databinding.FolderNameItemBinding

class FilesAdapter(
    private val folderName: String,
    private val files: List<File>,
    private val viewModel: FilesViewModel
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            R.layout.folder_name_item -> NameViewHolder(FolderNameItemBinding.inflate(inflater, parent, false))
            else -> ViewHolder(viewModel, inflater.inflate(viewType, parent, false))
        }
    }

    override fun getItemCount() = files.size + 1

    override fun getItemViewType(position: Int): Int {
        if(position == 0) return R.layout.folder_name_item
        return when(files[position - 1].type){
            "jpg", "jpeg" -> R.layout.file_image_item
            "mp4" -> R.layout.file_video_item
            else -> R.layout.file_image_item
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is ViewHolder -> {
                val item = files[position - 1]
                holder.bind(item)
            }
            is NameViewHolder -> holder.bind(folderName)
        }
    }

    // TODO create View Holder for image and video item
    class ViewHolder(
        private val viewModel: FilesViewModel,
        item: View
    ): RecyclerView.ViewHolder(item){

        private val image: ImageView = itemView.findViewById(R.id.file_view)

        fun bind(item: File){
//            itemView.layoutParams = FrameLayout.LayoutParams(item.width, item.height)
            val realPos = adapterPosition - 1
            image.setOnClickListener { viewModel.openFilesDetails(realPos) }

            Glide.with(itemView)
                .load(item.name)
                .placeholder(R.drawable.ic_image_24dp)
//                .override(item.width, item.height)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(image)

            val params = image.layoutParams
            if (params is FlexboxLayoutManager.LayoutParams){
                params.flexGrow = 1.0f
            }
        }
    }

    class NameViewHolder(private val binding: FolderNameItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(name: String){
            binding.rootFolder.text = name
        }
    }
}

class FilesPagerAdapter(
    private val viewModel: FilesViewModel
): PagingDataAdapter<File, FilesAdapter.ViewHolder>(FileDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilesAdapter.ViewHolder {
        return FilesAdapter.ViewHolder(viewModel, LayoutInflater.from(parent.context).inflate(R.layout.file_video_item, parent, false))
    }

    override fun onBindViewHolder(holder: FilesAdapter.ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)
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
