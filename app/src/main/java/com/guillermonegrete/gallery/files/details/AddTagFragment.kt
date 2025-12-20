package com.guillermonegrete.gallery.files.details

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.data.TagType
import com.guillermonegrete.gallery.databinding.FragmentAddTagBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import java.util.Date


@AndroidEntryPoint
class AddTagFragment: BottomSheetDialogFragment() {

    private val viewModel: AddTagViewModel by viewModels(extrasProducer = {
        defaultViewModelCreationExtras.withCreationCallback<AddTagViewModel.Factory> { factory ->
            factory.create(args.tagType, args.fileIds.toList(), args.tags.toSet())
        }
    })

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
        singleSelect = args.fileIds.size == 1

        setupViewModel()

        with(binding){

            val fileIds = args.fileIds
            adapter = TagSuggestionsAdapter { tag ->
                if(singleSelect) {
                    addChip(tag, fileIds.first())
                    if (newTagEdit.text.isNotEmpty()) newTagEdit.setText("")
                } else {
                    addTagToItems(tag, fileIds)
                }
            }

            if(singleSelect) {
                val fileId = fileIds.first()
                setEditKeyListener(fileId)
            }

            val layoutManager = FlexboxLayoutManager(context)
            savedTagsList.layoutManager = layoutManager
            savedTagsList.adapter = adapter

            newTagEdit.doAfterTextChanged {
                adapter.filter(it.toString())
            }

        }

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        object : BottomSheetDialog(requireContext(), theme) {
            override fun onAttachedToWindow() {
                super.onAttachedToWindow()

                ViewCompat.setOnApplyWindowInsetsListener(binding.savedTagsList) { view, insets ->
                    val sysInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                    view.updatePadding(bottom = sysInsets.bottom)
                    // Remove the default padding added to a material sheet, the padding is handled by the tag list instead
                    findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let {
                        it.post {
                            // Don't modify padding when the keyboard is visible, otherwise it doesn't align properly
                            val keyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
                            if (!keyboardVisible) it.updatePadding(bottom = 0)
                        }
                    }
                    insets
                }
            }
        }

    private fun addTagToItems(tag: Tag, ids: LongArray) {
        if (args.tagType == TagType.File) {
            disposable.add(
                viewModel.addTagToFiles(tag.id, ids.toList())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { files ->
                        setUpdatedItemsResult(tag, files.map { it.id })
                    },
                    { Timber.e(it, "Error adding tag to multiple files") }
                )
            )
        } else {
            disposable.add(viewModel.addTagToFolders(tag.id, ids.toList())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { folders ->
                        setUpdatedItemsResult(tag, folders.map { it.id })
                    },
                    { Timber.e(it, "Error adding tag to multiple folders") }
                )
            )
        }
    }

    private fun setUpdatedItemsResult(tag: Tag, updatedIds: List<Long>) {
        val bundle = bundleOf(SELECTED_TAG_KEY to tag, UPDATED_ITEMS_IDS_KEY to updatedIds.toLongArray())
        setFragmentResult(SELECT_TAG_REQUEST_KEY, bundle)
        dismiss()
    }

    private fun setEditKeyListener(fileId: Long) {

        val newTagEdit = binding.newTagEdit
        newTagEdit.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = v.text.toString()

                if (text.isBlank()) {
                    Snackbar.make(binding.root, "New tag can't be empty", Snackbar.LENGTH_SHORT).show()
                    return@setOnEditorActionListener true
                }

                newTagEdit.setText("")

                // if tag is already applied, skip
                if (viewModel.appliedTags.any { it.name == text }) return@setOnEditorActionListener true

                // Set id as 0 to indicate to the backend the tag is new
                addChip(Tag(text, Date(), 0), fileId)

                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun addAppliedTags(tags: List<Tag>) {
        if (!singleSelect) return
        val itemId = args.fileIds.first()
        val tagsGroup = binding.tagsGroup
        tagsGroup.removeViews(0, tagsGroup.childCount - 1)
        tags.forEach { tag ->
            val chip = Chip(context, null,  com.google.android.material.R.style.Widget_Material3_Chip_Input)
            chip.text = tag.name
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                removeChip(it, tag, itemId)
            }
            tagsGroup.addView(chip, tagsGroup.childCount - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
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
            // This makes sure only the current listening fragments get the result, avoids other fragments getting it later when the result is no longer valid
            clearFragmentResult(REQUEST_KEY)
        }
    }

    private fun setupViewModel() {
        disposable.add(viewModel.tags
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    // Remove tags that are already applied
                    val newList = it.toMutableSet()
                    newList.removeAll(viewModel.appliedTags)
                    adapter.modifyList(newList)
                    addAppliedTags(viewModel.appliedTags.toList())
                }, Timber::e
            )
        )

        // Load the initial tags
        disposable.add(viewModel.itemTags
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { tags ->
                    addAppliedTags(tags.toList())
                    adapter.removeItems(tags)
                },
                Timber::e
            )
        )
    }

    /**
     * Call the backend to create/add a [tag] to the [fileId]. If successful add a new chip to the to tags group.
     */
    private fun addChip(tag: Tag, fileId: Long) {

        val tagTarget =
            if (args.tagType == TagType.File) viewModel.addTag(fileId, tag) else viewModel.addFolderTag(fileId, tag)
        disposable.add(tagTarget
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ newTag ->
                // No longer show the tag in the suggestions list
                adapter.remove(newTag)
            }, {
                Snackbar.make(binding.root, "Error: Couldn't add tag", Snackbar.LENGTH_SHORT).show()
                Timber.e(it, "Couldn't add tag: $tag")
            })
        )
    }

    private fun removeChip(chipView: View, tag: Tag, fileId: Long) {

        val tagTarget = if (args.tagType == TagType.File) viewModel.deleteTagFromFile(fileId, tag) else viewModel.deleteTagFromFolder(fileId, tag)
        disposable.add(tagTarget
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
        const val UPDATED_ITEMS_IDS_KEY = "updated_items_ids"
    }

}
