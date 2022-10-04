package com.guillermonegrete.gallery.files.details

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.ImageFile
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.data.VideoFile
import com.guillermonegrete.gallery.files.FileDiffCallback
import io.reactivex.rxjava3.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.*


class FileDetailsAdapter: PagingDataAdapter<File, FileDetailsAdapter.ViewHolder>(FileDiffCallback){

    val panelTouchSubject: PublishSubject<Boolean> = PublishSubject.create()

    private val formatter = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())

    private var isSheetVisible = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when(viewType){
            R.layout.file_details_image_item -> ImageViewHolder(layout)
            R.layout.file_details_video_item -> VideoViewHolder(layout)
            else -> ImageViewHolder(layout)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is ImageFile -> R.layout.file_details_image_item
            is VideoFile -> R.layout.file_details_video_item
            else -> R.layout.file_details_image_item
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = getItem(position) ?: return
        holder.bind(file)
    }

    abstract inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        private val nameText: TextView = itemView.findViewById(R.id.file_name_text)
        private val linkButton: ImageButton = itemView.findViewById(R.id.open_link_button)
        private val fileSizeText: TextView = itemView.findViewById(R.id.file_size)
        private val createdText: TextView = itemView.findViewById(R.id.creation_date)
        private val modifiedText: TextView = itemView.findViewById(R.id.modified_date)
        private val bottomSheet: LinearLayout = itemView.findViewById(R.id.bottom_layout)

        private val editButton: ImageButton = itemView.findViewById(R.id.edit_btn)
        private val tagGroups: ChipGroup = itemView.findViewById(R.id.tags_chip_group)

        private val behaviour = BottomSheetBehavior.from(bottomSheet)

        init {
            setSheets()
        }

        @SuppressLint("ClickableViewAccessibility")
        fun setSheets(){
            // We need to return true so the bottom sheet handles the touch events.
            // We only do this because otherwise PhotoView consumes the events.
            bottomSheet.setOnTouchListener { _, _ -> true }

            // Hidden state is not considered because it's not enabled for this bottom sheet
            behaviour.setBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback(){
                @SuppressLint("NotifyDataSetChanged")
                override fun onStateChanged(p0: View, p1: Int) {
                    when(p1){
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            panelTouchSubject.onNext(false)
                            isSheetVisible = false
                            notifyDataSetChanged()
                        }
                        BottomSheetBehavior.STATE_EXPANDED  -> {
                            panelTouchSubject.onNext(false)
                            isSheetVisible = true
                            notifyDataSetChanged()
                        }
                        BottomSheetBehavior.STATE_SETTLING -> panelTouchSubject.onNext(false)
                        BottomSheetBehavior.STATE_DRAGGING -> panelTouchSubject.onNext(true)
                        else -> {}
                    }
                }

                override fun onSlide(p0: View, p1: Float) {}
            })
        }

        open fun bind(file: File){
            nameText.text = file.filename
            fileSizeText.text = file.sizeText
            createdText.text = formatter.format(file.creationDate)
            modifiedText.text = formatter.format(file.lastModified)
            behaviour.state = if(isSheetVisible) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
            linkButton.setOnClickListener { openLink(file.name) }

            editButton.setOnClickListener {
                val action = FileDetailsFragmentDirections.fileDetailsToAddTagFragment(file.id, file.tags.toTypedArray())
                itemView.findNavController().navigate(action)
            }

            setTags(file.tags)
        }

        private fun setTags(tags: List<Tag>) {
            tagGroups.removeAllViews()
            tags.forEach {
                val chip =  Chip(itemView.context)
                chip.text = it.name
                tagGroups.addView(chip)
            }
        }

        private fun openLink(item: String){
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(item)
            }
            itemView.context.startActivity(intent)
        }
    }

    inner class ImageViewHolder(itemView: View): ViewHolder(itemView){

        private val fileImage: ImageView = itemView.findViewById(R.id.file_image)

        override fun bind(file: File){
            super.bind(file)
            Glide.with(itemView.context)
                .load(file.name)
                .into(fileImage)
            // fit_start places the image at the top, fit_center is the default
            fileImage.scaleType = if(isSheetVisible) ImageView.ScaleType.FIT_START else ImageView.ScaleType.FIT_CENTER
        }
    }

    inner class VideoViewHolder(itemView: View): ViewHolder(itemView){

        private val player: View = itemView.findViewById(R.id.exo_player_view)

        override fun bind(file: File) {
            super.bind(file)
            player.layoutParams = player.layoutParams.apply {
                height = if(isSheetVisible) ViewGroup.LayoutParams.WRAP_CONTENT else ViewGroup.LayoutParams.MATCH_PARENT
            }
        }
    }
}