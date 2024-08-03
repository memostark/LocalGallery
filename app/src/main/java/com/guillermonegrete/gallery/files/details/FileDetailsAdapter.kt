package com.guillermonegrete.gallery.files.details

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoViewAttacher
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
import kotlin.math.abs


class FileDetailsAdapter: PagingDataAdapter<File, FileDetailsAdapter.ViewHolder>(FileDiffCallback){

    val panelTouchSubject: PublishSubject<Boolean> = PublishSubject.create()

    val setCoverSubject: PublishSubject<Long> = PublishSubject.create()

    val onImageTapSubject: PublishSubject<Boolean> = PublishSubject.create()

    private val formatter = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())

    var isSheetVisible = false

    var isAllFilesDest = false

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

    @SuppressLint("NotifyDataSetChanged")
    fun showSheet() {
        if(!isSheetVisible) {
            isSheetVisible = true
            notifyDataSetChanged()
        }
    }

    abstract inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        private val nameText: TextView = itemView.findViewById(R.id.file_name_text)
        private val folderText: TextView = itemView.findViewById(R.id.folder_text)
        private val linkButton: ImageButton = itemView.findViewById(R.id.open_link_button)
        private val folderButton: ImageButton = itemView.findViewById(R.id.open_folder_button)
        private val fileSizeText: TextView = itemView.findViewById(R.id.file_size)
        private val createdText: TextView = itemView.findViewById(R.id.creation_date)
        private val modifiedText: TextView = itemView.findViewById(R.id.modified_date)
        private val bottomSheet: LinearLayout = itemView.findViewById(R.id.bottom_layout)

        private val editButton: ImageButton = itemView.findViewById(R.id.edit_btn)
        private val setCoverButton: ImageButton = itemView.findViewById(R.id.set_cover_btn)
        private val tagGroups: ChipGroup = itemView.findViewById(R.id.tags_chip_group)

        protected val behaviour = BottomSheetBehavior.from(bottomSheet)

        init {
            setSheets()
            setCoverButton.isGone = isAllFilesDest
        }

        @SuppressLint("ClickableViewAccessibility")
        fun setSheets(){
            // We need to return true so the bottom sheet handles the touch events.
            // We only do this because otherwise PhotoView consumes the events.
            bottomSheet.setOnTouchListener { _, _ -> true }

            // Hidden state is not considered because it's not enabled for this bottom sheet
            behaviour.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback(){
                override fun onStateChanged(p0: View, p1: Int) {
                    when(p1){
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            isSheetVisible = false
                            panelTouchSubject.onNext(false)
                        }
                        BottomSheetBehavior.STATE_EXPANDED  -> {
                            isSheetVisible = true
                            panelTouchSubject.onNext(false)
                        }
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
                val action = FileDetailsFragmentDirections.fileDetailsToAddTagFragment(longArrayOf(file.id), file.tags.toTypedArray())
                itemView.findNavController().navigate(action)
            }

            setCoverButton.setOnClickListener {
                setCoverSubject.onNext(file.id)
            }

            val folder = file.folder
            folderText.isVisible = folder != null
            folderButton.isVisible = folder != null
            if (folder != null) {
                folderText.text = folder.name
                folderButton.setOnClickListener {
                    val action = FileDetailsFragmentDirections.fileDetailsToFilesFragment(folder)
                    itemView.findNavController().navigate(action)
                }
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

        init {
            val attacher = PhotoViewAttacher(fileImage)
            attacher.setOnSingleFlingListener { e1, e2, _, velocityY ->
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                // Detects upwards vertical swipe (distance and speed are negative)
                if (abs(diffY) > abs(diffX)) {
                    if (diffY < -Companion.SWIPE_THRESHOLD && velocityY < -Companion.SWIPE_VELOCITY_THRESHOLD) {
                        // State is false, change to true so when it reaches the expanded state the new false state is processed
                        panelTouchSubject.onNext(true)
                        behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                        return@setOnSingleFlingListener true
                    }
                }
                return@setOnSingleFlingListener false
            }
            attacher.setOnViewTapListener { view, x, y ->
                onImageTapSubject.onNext(true)
            }
        }

        override fun bind(file: File){
            super.bind(file)
            Glide.with(itemView.context)
                .load(file.name)
                .into(fileImage)
            fileImage.layoutParams = fileImage.layoutParams.apply {
                height = if(isSheetVisible) ViewGroup.LayoutParams.WRAP_CONTENT else ViewGroup.LayoutParams.MATCH_PARENT
            }
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

    companion object {
        const val SWIPE_THRESHOLD = 0.8
        const val SWIPE_VELOCITY_THRESHOLD = 0.8
    }
}