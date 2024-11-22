package com.guillermonegrete.gallery.files.details

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.databinding.FragmentAddTagBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class AddTagFragment: BottomSheetDialogFragment() {

    private val viewModel: AddTagViewModel by viewModels()

    private  var _binding: FragmentAddTagBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TagSuggestionsAdapter

    private val disposable = CompositeDisposable()

    private val args: AddTagFragmentArgs by navArgs()

    private var singleSelect = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogHiddenKeyboardStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTagBinding.inflate(inflater, container, false)

        // if the origin destination is the files, it means this is a file multi selection
        singleSelect = findNavController().previousBackStackEntry?.destination?.id == R.id.file_details_dest

        setupViewModel()

        with(binding){

            val fileIds = args.fileIds
            adapter = TagSuggestionsAdapter { tag ->
                if(singleSelect) {
                    addChip(tag, fileIds.first())
                    if (newTagEdit.text.isNotEmpty()) newTagEdit.setText("")
                } else {
                    addTagToFiles(tag, fileIds)
                }
            }

            if(singleSelect) {
                val tags = viewModel.appliedTags
                val fileId = fileIds.first()
                addAppliedTags(tags, fileId)
                setEditKeyListener(tags, fileId)
            }

            savedTagsList.adapter = adapter

            newTagEdit.doAfterTextChanged {
                adapter.filter(it.toString())
            }

        }

        return binding.root
    }

    private fun addTagToFiles(tag: Tag, fileIds: LongArray) {
        disposable.add(viewModel.addTagToFiles(tag.id, fileIds.toList())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { files ->
                    val ids = files.map { it.id }.toLongArray()
                    val bundle = bundleOf(SELECTED_TAG_KEY to tag, UPDATED_FILES_IDS_KEY to ids)
                    setFragmentResult(SELECT_TAG_REQUEST_KEY, bundle)
                    dismiss()
                },
                { Timber.e(it, "Error adding tag to multiple files") }
            )
        )
    }

    private fun setEditKeyListener(tags: Set<Tag>, fileId: Long) {

        val newTagEdit = binding.newTagEdit
        newTagEdit.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = v.text.toString()
                newTagEdit.setText("")

                // if tag is already applied, skip
                if (tags.any { it.name == text }) return@setOnEditorActionListener true

                // Set id as 0 to indicate to the backend the tag is new
                addChip(Tag(text, Date(), 0), fileId)

                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun addAppliedTags(tags: Set<Tag>, fileId: Long) {
        val tagsGroup = binding.tagsGroup
        tags.forEach { tag ->
            val chip = Chip(context)
            chip.text = tag.name
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                removeChip(it, tag, fileId)
            }
            tagsGroup.addView(chip, tagsGroup.childCount - 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        // Don't set result listener when dealing with multiple ids
        if (singleSelect) {
            setFragmentResult(
                REQUEST_KEY,
                Bundle().apply { putParcelableArrayList(TAGS_KEY, ArrayList(viewModel.appliedTags)) }
            )
        }
    }

    private fun setupViewModel() {
        viewModel.appliedTags.addAll(args.tags)

        disposable.add(viewModel.getAllTags()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    // Remove tags that are already applied
                    val newList = it.toMutableSet()
                    newList.removeAll(args.tags.toSet())
                    adapter.modifyList(newList)
                }, { Timber.e(it) }
            )
        )
    }

    /**
     * Call the backend to create/add a [tag] to the [fileId]. If successful add a new chip to the to tags group.
     */
    private fun addChip(tag: Tag, fileId: Long) {
        val tagsGroup = binding.tagsGroup

        disposable.add(viewModel
            .addTag(fileId, tag)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ newTag ->
                // Creates and adds chip view
                val chip =  Chip(context)
                chip.text = newTag.name
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener { view ->
                    removeChip(view, newTag, fileId)
                }
                tagsGroup.addView(chip, tagsGroup.childCount - 1)

                // No longer show the tag in the suggestions list
                adapter.remove(newTag)
            }, { Timber.e(it) })
        )
    }

    private fun removeChip(chipView: View, tag: Tag, fileId: Long) {

        disposable.add(
            viewModel.deleteTagFromFile(fileId, tag)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    binding.tagsGroup.removeView(chipView)
                    adapter.add(tag)
                   }, { Timber.e(it) }
                )
        )
    }

    companion object{
        // Adding tags to a single file
        const val TAGS_KEY = "tags_key"
        const val REQUEST_KEY = "tag_frag_request"

        // For adding a tag to multiple files
        const val SELECT_TAG_REQUEST_KEY = "select_tag_request"
        const val SELECTED_TAG_KEY = "selected_tag"
        const val UPDATED_FILES_IDS_KEY = "updated_files_ids"
    }

}
