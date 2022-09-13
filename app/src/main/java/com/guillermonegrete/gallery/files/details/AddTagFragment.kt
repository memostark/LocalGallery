package com.guillermonegrete.gallery.files.details

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.guillermonegrete.gallery.MyApplication
import com.guillermonegrete.gallery.databinding.FragmentAddTagBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class AddTagFragment: BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<AddTagViewModel> { viewModelFactory }

    private lateinit var adapter: TagSuggestionsAdapter

    private val disposable = CompositeDisposable()

    private val args: AddTagFragmentArgs by navArgs()

    override fun onAttach(context: Context) {
        (context.applicationContext as MyApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAddTagBinding.inflate(inflater, container, false)

        setupViewModel()

        with(binding){
            val tags = args.tags.toList()
            tags.forEach { tag ->
                val chip =  Chip(context)
                chip.text = tag.name
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    tagsGroup.removeView(it)
                }
                tagsGroup.addView(chip, tagsGroup.childCount - 1)
            }

            adapter = TagSuggestionsAdapter { tag, adapter ->
                addChip(tagsGroup, tag.name)
                adapter.remove(tag)
            }

            newTagEdit.setOnEditorActionListener { v, actionId, _ ->
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    val text = v.text.toString()
                    newTagEdit.setText("")

                    // if tag is already applied, skip
                    if(tags.any { it.name == text }) return@setOnEditorActionListener true

                    addChip(tagsGroup, text)

                    adapter.getUnfilteredList().removeAll { it.name == text }
                    adapter.modifyList(adapter.getUnfilteredList())

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

    private fun setupViewModel() {
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

    private fun addChip(tagsGroup: ChipGroup, name: String) {
        val chip =  Chip(context)
        chip.text = name
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            tagsGroup.removeView(it)
        }
        tagsGroup.addView(chip, tagsGroup.childCount - 1)
    }
}
