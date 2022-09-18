package com.guillermonegrete.gallery.files.details

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.guillermonegrete.gallery.MyApplication
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.databinding.FragmentAddTagBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class AddTagFragment: BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<AddTagViewModel> { viewModelFactory }

    private  var _binding: FragmentAddTagBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TagSuggestionsAdapter

    private val disposable = CompositeDisposable()

    private val args: AddTagFragmentArgs by navArgs()

    override fun onAttach(context: Context) {
        (context.applicationContext as MyApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This style avoids content being displayed behind the keyboard
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AdjustResizeDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTagBinding.inflate(inflater, container, false)

        setupViewModel()

        with(binding){
            val tags = viewModel.appliedTags
            tags.forEach { tag ->
                val chip =  Chip(context)
                chip.text = tag.name
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    removeChip(it, tag)
                }
                tagsGroup.addView(chip, tagsGroup.childCount - 1)
            }

            adapter = TagSuggestionsAdapter { tag ->
                addChip(tagsGroup, tag)
            }

            newTagEdit.setOnEditorActionListener { v, actionId, _ ->
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    val text = v.text.toString()
                    newTagEdit.setText("")

                    // if tag is already applied, skip
                    if(tags.any { it.name == text }) return@setOnEditorActionListener true

                    addChip(tagsGroup, Tag(text, Date(), 0)) // Set id as 0 to indicate to the backend the tag is new

                    return@setOnEditorActionListener true
                }
                false
            }

            savedTagsList.adapter = adapter

            newTagEdit.doAfterTextChanged {
                adapter.filter(it.toString())
            }

        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        setFragmentResult(
            REQUEST_KEY,
            Bundle().apply {
                putParcelableArrayList(TAGS_KEY, ArrayList(viewModel.appliedTags))
            }
        )
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
     * Call the backend to create/add a [tag] to the file. If successful add a new chip to the to [tagsGroup]
     */
    private fun addChip(tagsGroup: ChipGroup, tag: Tag) {

        disposable.add(viewModel
            .addTag(args.fileId, tag)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ newTag ->
                // Creates and adds chip view
                val chip =  Chip(context)
                chip.text = newTag.name
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener { view ->
                    removeChip(view, newTag)
                }
                tagsGroup.addView(chip, tagsGroup.childCount - 1)

                // No longer show the tag in the suggestions list
                adapter.remove(newTag)
            }, { Timber.e(it) })
        )
    }

    private fun removeChip(chipView: View, tag: Tag) {

        disposable.add(
            viewModel.deleteTagFromFile(args.fileId, tag)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    binding.tagsGroup.removeView(chipView)
                    adapter.add(tag)
                   }, { Timber.e(it) }
                )
        )
    }

    companion object{
        const val TAGS_KEY = "tags_key"
        const val REQUEST_KEY = "tag_frag_request"
    }

}
