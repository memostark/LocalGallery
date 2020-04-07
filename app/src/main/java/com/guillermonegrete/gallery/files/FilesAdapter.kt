package com.guillermonegrete.gallery.files

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxLayoutManager
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.File

class FilesAdapter(
    private val folderName: String,
    private val files: List<File>,
    private val viewModel: FilesViewModel
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when(viewType) {
            R.layout.folder_name_item -> NameViewHolder(item)
            else -> ViewHolder(viewModel, item)
        }
    }

    override fun getItemCount() = files.size

    override fun getItemViewType(position: Int): Int {
        if(position == 0) return R.layout.folder_name_item
        return when(files[position].type){
            "jpg", "jpeg" -> R.layout.file_image_item
            "mp4" -> R.layout.file_video_item
            else -> R.layout.file_image_item
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = files[position]
        when(holder){
            is ViewHolder -> holder.bind(item)
            is NameViewHolder -> holder.bind(folderName)
        }
    }

    class ViewHolder(
        private val viewModel: FilesViewModel,
        item: View
    ): RecyclerView.ViewHolder(item){

        private val image: ImageView = itemView.findViewById(R.id.file_view)

        fun bind(item: File){
            image.setOnClickListener { viewModel.openFilesDetails(adapterPosition) }

            Glide.with(itemView)
                .load(item.name)
                .placeholder(R.drawable.ic_image_24dp)
                .apply( RequestOptions().override(200, 400))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(image)

            val params = image.layoutParams
            if (params is FlexboxLayoutManager.LayoutParams){
                params.flexGrow = 1.0f
            }
        }
    }

    class NameViewHolder(item: View): RecyclerView.ViewHolder(item){

        private val folderName: TextView = itemView.findViewById(R.id.textView_root_folder)

        fun bind(name: String){
            folderName.text = name
        }
    }
}