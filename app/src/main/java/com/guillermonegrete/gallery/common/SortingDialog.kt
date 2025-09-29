package com.guillermonegrete.gallery.common

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.guillermonegrete.gallery.NavGraphDirections
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.data.TagType
import com.guillermonegrete.gallery.databinding.ChoiceChipBinding
import com.guillermonegrete.gallery.databinding.DialogFileOrderByBinding
import com.guillermonegrete.gallery.files.SortField
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@AndroidEntryPoint
class SortingDialog: BottomSheetDialogFragment() {

    private val args: SortingDialogArgs by navArgs()

    val viewModel: SortingDialogViewModel by viewModels(extrasProducer = {
        defaultViewModelCreationExtras.withCreationCallback<SortingDialogViewModel.Factory> { factory ->
            factory.create(args.folderId)
        }
    })
    private val disposable = CompositeDisposable()

    private var checkedTagIds = mutableSetOf<Long>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DialogFileOrderByBinding.inflate(inflater, container, false)

        with(binding){

            val options = args.options
            if (options != null) {
                fieldSort.removeAllViews()
                options.map { option ->
                    val rb = RadioButton(ContextThemeWrapper(context, R.style.SortDialogRadioButton)).apply {
                        text = option.display
                        id = option.id
                    }
                    fieldSort.addView(rb)
                }
            }

            val selections = args.selections
            var checkedField = selections.field
            var checkedOrder = selections.sort
            fieldSort.check(checkedField.id)
            orderSort.check(checkedOrder.id)

            fieldSort.setOnCheckedChangeListener { _, i ->
                checkedField = SortField.fromId(i)
            }

            orderSort.setOnCheckedChangeListener { _, i ->
                checkedOrder = Order.fromInteger(i)
            }

            doneButton.setOnClickListener {
                dismiss()
                if(selections.field != checkedField || selections.sort != checkedOrder || selections.tagIds != checkedTagIds) {
                    setFragmentResult(RESULT_KEY, bundleOf(SORT_KEY to SortDialogChecked(checkedField, checkedOrder, checkedTagIds.toList())))
                }
            }

            clearTagsButton.setOnClickListener {
                tagsGroup.clearCheck()
                folderTagsGroup.clearCheck()
            }

            tagsGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                clearTagsButton.isVisible = checkedIds.isNotEmpty() || folderTagsGroup.checkedChipIds.isNotEmpty()
            }
            folderTagsGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                clearTagsButton.isVisible = checkedIds.isNotEmpty() || tagsGroup.checkedChipIds.isNotEmpty()
            }

            // handle tags
            val folderId = args.folderId
            val folderList = folderId == FOLDER_TAGS
            val isSingleFolder = !folderList && folderId != GET_ALL_TAGS

            var hasFileTag = false
            var hasFolderTag = false

            val folderTagIds = mutableListOf<Tag>()
            binding.addTagBtn.isVisible = isSingleFolder
            binding.addTagBtn.setOnClickListener {
                val action = NavGraphDirections.globalToAddTagFragment(longArrayOf(folderId), folderTagIds.toTypedArray(), TagType.Folder)
                findNavController().navigate(action)
            }

            disposable.add(viewModel.tags
                .subscribe(
                    { tags ->
                        chipScroll.isVisible = true
                        folderChipScroll.isVisible = true
                        tags.forEach { tag ->
                            val chip =  ChoiceChipBinding.inflate(LayoutInflater.from(context)).root
                            val isFileTag = tag.type == TagType.File
                            if (isFileTag) {
                                hasFileTag = true
                            } else {
                                hasFolderTag = true
                                folderTagIds.add(tag)
                            }
                            val text = if (folderList || isFileTag) "${tag.name} (${tag.count})" else tag.name
                            chip.id = tag.id.toInt()
                            chip.text = text
                            if (tag.id in args.selections.tagIds) {
                                checkedTagIds.add(tag.id)
                                chip.isChecked = true
                                clearTagsButton.isVisible = true
                            }
                            // All file tags are checkable and folder tags are only not checkable when viewing a single folder
                            chip.isCheckable = isFileTag || !isSingleFolder
                            chip.setOnCheckedChangeListener { _, isChecked ->
                                if (isChecked) checkedTagIds.add(tag.id) else checkedTagIds.remove(tag.id)
                            }
                            if (isFileTag) tagsGroup.addView(chip) else folderTagsGroup.addView(chip)
                        }
                        tagsSeparator.isVisible = hasFileTag && hasFolderTag
                    },
                    Timber::e
                )
            )
        }


        return binding.root
    }

    override fun onDestroyView() {
        disposable.clear()
        super.onDestroyView()
    }

    companion object{
        const val RESULT_KEY = "sort_dialog_result"
        const val SORT_KEY = "sort_key"

        /**
         * Used to indicate to get all the tags instead of just the tags of a specific folder
         */
        const val GET_ALL_TAGS = -1L
        const val FOLDER_TAGS = 0L
    }
}

enum class Order(val id: Int, val oder: String){
    ASC(R.id.ascending_order, "asc"),
    DESC(R.id.descending_order, "desc");

    companion object {

        private val values = Order.entries

        fun fromInteger(id: Int): Order {
            return values.firstOrNull { it.id == id} ?: DEFAULT
        }

        fun fromOrder(order: String?): Order {
            return values.firstOrNull { it.oder == order} ?: DEFAULT
        }

        val DEFAULT = ASC
    }
}

/**
 * Class used to pass the [display] text and [id] for the RadioButton.
 *
 * Because enums can't be passed through nav args this class is required.
 */
@Parcelize
data class Field(val display: String, val id: Int): Parcelable

@Parcelize
data class SortDialogChecked(
    val field: SortField,
    val sort: Order,
    val tagIds: List<Long> = emptyList(),
): Parcelable {

    companion object{
        val DEFAULT_FILE = SortDialogChecked(SortField.DEFAULT, Order.DEFAULT)
        val DEFAULT_FOLDER = SortDialogChecked(SortField.DEFAULT_FOLDER, Order.DEFAULT)
    }
}
