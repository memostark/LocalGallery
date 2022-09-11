package com.guillermonegrete.gallery.files.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.databinding.FragmentAddTagBinding
import java.util.*

class AddTagFragment: BottomSheetDialogFragment() {

    private val args: AddTagFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAddTagBinding.inflate(inflater, container, false)

        val dummySuggestions = mutableSetOf(
            Tag("option", Date(), 0),
            Tag("suggestion", Date(), 1),
            Tag("recommendation", Date(), 2),
        )

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

            val adapter = TagSuggestionsAdapter { tag, adapter ->
                addChip(tagsGroup, tag.name)
                dummySuggestions.remove(tag)
                adapter.modifyList(dummySuggestions.toList())
            }

            newTagEdit.setOnEditorActionListener { v, actionId, _ ->
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    val text = v.text.toString()
                    newTagEdit.setText("")

                    // if tag is already applied, skip
                    if(tags.any { it.name == text }) return@setOnEditorActionListener true

                    addChip(tagsGroup, text)

                    dummySuggestions.removeAll { it.name == text }
                    adapter.modifyList(dummySuggestions.toList())

                    return@setOnEditorActionListener true
                }
                false
            }

            adapter.currentList

            adapter.modifyList(dummySuggestions.toList())
            savedTagsList.adapter = adapter

            newTagEdit.doAfterTextChanged {
                adapter.filter(it.toString())
            }

        }

        return binding.root
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
