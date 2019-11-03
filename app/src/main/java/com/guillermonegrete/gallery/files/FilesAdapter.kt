package com.guillermonegrete.gallery.files

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.File

class FilesAdapter(
    private val files: List<String>,
    private val viewModel: FilesViewModel
): RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.file_item, parent, false)
        return ViewHolder(viewModel, item)
    }

    override fun getItemCount() = files.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = files[position]
        holder.bind(item)
    }

    class ViewHolder(
        private val viewModel: FilesViewModel,
        item: View
    ): RecyclerView.ViewHolder(item){

        private val image: ImageView = itemView.findViewById(R.id.file_view)

        fun bind(item: String){
            image.setOnClickListener { viewModel.openFilesDetails(File(item)) }

            Glide.with(itemView)
                .load(item)
                .placeholder(R.drawable.ic_image_24dp)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(image)
        }
    }
}