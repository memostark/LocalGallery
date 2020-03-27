package com.guillermonegrete.gallery.files.details

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.File


class FileDetailsAdapter(val files : List<File>): RecyclerView.Adapter<FileDetailsAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.file_details_item, parent, false)
        return ViewHolder(layout)
    }

    override fun getItemCount() = files.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(files[position])
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        private val fileImage: ImageView = itemView.findViewById(R.id.file_image)
        val nameText: TextView = itemView.findViewById(R.id.file_name_text)
        val linkButton: ImageButton = itemView.findViewById(R.id.open_link_button)
        private val bottomSheet: LinearLayout = itemView.findViewById(R.id.bottom_layout)

        init {
            enableSheets()
        }

        /**
         * We need to return true so the bottom sheet handles the touch events.
         * We only do this because otherwise PhotoView consumes the events.
         */
        @SuppressLint("ClickableViewAccessibility")
        fun enableSheets(){
            bottomSheet.setOnTouchListener { _, _ ->
                return@setOnTouchListener true
            }
        }

        fun bind(file: File){
            Glide.with(itemView.context)
                .load(file.name)
                .into(fileImage)

            nameText.text = file.name
            linkButton.setOnClickListener { openLink(file.name) }
        }

        private fun openLink(item: String){
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(item)
            }
            itemView.context.startActivity(intent)
        }
    }

}