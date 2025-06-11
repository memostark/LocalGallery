package com.guillermonegrete.gallery.files.details

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.OptIn
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
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
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.ImageFile
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.data.VideoFile
import com.guillermonegrete.gallery.files.FileDiffCallback
import io.reactivex.rxjava3.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class FileDetailsAdapter: PagingDataAdapter<File, FileDetailsAdapter.ViewHolder>(FileDiffCallback){

    val panelTouchSubject: PublishSubject<Int> = PublishSubject.create()

    val setCoverSubject: PublishSubject<Long> = PublishSubject.create()

    val onImageTapSubject: PublishSubject<Boolean> = PublishSubject.create()

    val onFolderIconTap: PublishSubject<Folder> = PublishSubject.create()

    private val formatter = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())

    var isSheetVisible = false
    var showControls = true

    var isAllFilesDest = false

    var bottomInset = 0
    var chipPadding = 0f
    var addTagText = ""

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val context = recyclerView.context
        chipPadding = context.resources.getDimension(R.dimen.chip_text_padding)
        addTagText = context.resources.getString(R.string.add_tag)
    }

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

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any?>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            for (payload in payloads) {
                when (payload) {
                    PAYLOAD_BOTTOM_INSET -> holder.handleInsets()
                    PAYLOAD_SHEET_STATE -> holder.updateBottomSheet()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSheet(visibility: Boolean) {
        if(visibility != isSheetVisible) {
            isSheetVisible = visibility
            notifyDataSetChanged()
        }
    }

    fun updateInsets(inset: Int) {
        bottomInset = inset
        notifyItemRangeChanged(0, itemCount, PAYLOAD_BOTTOM_INSET)
    }

    fun notifySheetChange() {
        notifyItemRangeChanged(0, itemCount, PAYLOAD_SHEET_STATE)
    }

    abstract inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        private val nameText: TextView = itemView.findViewById(R.id.file_name_text)
        private val folderText: TextView = itemView.findViewById(R.id.folder_text)
        private val linkButton: ImageButton = itemView.findViewById(R.id.open_link_button)
        private val folderButton: ImageButton = itemView.findViewById(R.id.open_folder_button)
        private val fileSizeText: TextView = itemView.findViewById(R.id.file_size)
        private val createdText: TextView = itemView.findViewById(R.id.creation_date)
        private val modifiedText: TextView = itemView.findViewById(R.id.modified_date)
        private val bottomSheet: ViewGroup = itemView.findViewById(R.id.bottom_layout)

        private val setCoverButton: ImageButton = itemView.findViewById(R.id.set_cover_btn)
        private val tagGroup: ChipGroup = itemView.findViewById(R.id.tags_chip_group)
        private val addTag: Chip = itemView.findViewById(R.id.add_tag_btn)

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
                    panelTouchSubject.onNext(p1)
                }

                override fun onSlide(p0: View, p1: Float) {}
            })


        }

        open fun bind(file: File){
            nameText.text = file.filename
            fileSizeText.text = file.sizeText
            createdText.text = formatter.format(file.creationDate)
            modifiedText.text = formatter.format(file.lastModified)
            bottomSheet.updatePadding(bottom = if (isSheetVisible) bottomInset else 0)
            bottomSheet.post { behaviour.state = if(isSheetVisible) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED }
            linkButton.setOnClickListener { openLink(file.name) }

            addTag.setOnClickListener {
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
                    onFolderIconTap.onNext(folder)
                }
            }

            setTags(file.tags)
            updateLayout()
        }

        abstract fun updateLayout()

        private fun setTags(tags: List<Tag>) {
            setAddTagButton(tags.isEmpty())
            tagGroup.removeViews(1, tagGroup.childCount - 1)
            tags.forEach {
                val chip =  Chip(itemView.context)
                chip.text = it.name
                tagGroup.addView(chip)
            }
        }

        private fun setAddTagButton(isIcon: Boolean) {
            if (isIcon) {
                addTag.text = addTagText
                addTag.textEndPadding = chipPadding
                addTag.textStartPadding = chipPadding
                addTag.iconEndPadding = 0f
                addTag.iconStartPadding = 0f
            } else {
                addTag.text = null
                addTag.textEndPadding = 0f
                addTag.textStartPadding = 0f
                addTag.iconEndPadding = chipPadding
                addTag.iconStartPadding = chipPadding
                addTag.updatePadding(left = 0, right = 0)
            }
        }

        private fun openLink(item: String){
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(item)
            }
            itemView.context.startActivity(intent)
        }

        fun handleInsets() {
            bottomSheet.updatePadding(bottom = if (isSheetVisible) bottomInset else 0)
        }

        fun updateBottomSheet() {
            bottomSheet.updatePadding(bottom = if (isSheetVisible) bottomInset else 0)
            bottomSheet.post { behaviour.state = if(isSheetVisible) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED }
            updateLayout()
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
                    if (diffY < -SWIPE_THRESHOLD && velocityY < -SWIPE_VELOCITY_THRESHOLD) {
                        // State is false, change to true so when it reaches the expanded state the new false state is processed
                        panelTouchSubject.onNext(BottomSheetBehavior.STATE_EXPANDED)
                        return@setOnSingleFlingListener true
                    }
                }
                return@setOnSingleFlingListener false
            }
            attacher.setOnViewTapListener { _, _, _ ->
                onImageTapSubject.onNext(true)
            }
        }

        override fun bind(file: File){
            super.bind(file)
            Glide.with(itemView.context)
                .load(file.name)
                .into(fileImage)
        }

        override fun updateLayout() {
            fileImage.layoutParams = fileImage.layoutParams.apply {
                height = if(isSheetVisible) ViewGroup.LayoutParams.WRAP_CONTENT else ViewGroup.LayoutParams.MATCH_PARENT
            }
        }
    }

    inner class VideoViewHolder(itemView: View): ViewHolder(itemView){

        private val player: PlayerView = itemView.findViewById(R.id.exo_player_view)

        @OptIn(UnstableApi::class)
        override fun bind(file: File) {
            super.bind(file)
            player.controllerAutoShow = showControls
            player.updatePadding(bottom = if (showControls) bottomInset else 0)
        }

        override fun updateLayout() {
            player.layoutParams = player.layoutParams.apply {
                height = if(isSheetVisible) ViewGroup.LayoutParams.WRAP_CONTENT else ViewGroup.LayoutParams.MATCH_PARENT
            }
        }
    }

    companion object {
        const val SWIPE_THRESHOLD = 0.8
        const val SWIPE_VELOCITY_THRESHOLD = 0.8

        const val PAYLOAD_BOTTOM_INSET = "bottom_inset"
        const val PAYLOAD_SHEET_STATE = "sheet_visibility"
    }
}