package com.guillermonegrete.gallery.common

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.guillermonegrete.gallery.NavGraphDirections
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.data.TagType
import com.guillermonegrete.gallery.data.source.remote.FilterTags
import com.guillermonegrete.gallery.databinding.DialogFileOrderByBinding
import com.guillermonegrete.gallery.files.SortField
import com.guillermonegrete.gallery.files.details.AddTagFragment
import com.guillermonegrete.gallery.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.util.Date

@AndroidEntryPoint
class SortingDialog: BottomSheetDialogFragment() {

    private  var _binding: DialogFileOrderByBinding? = null
    private val binding get() = _binding!!

    private val args: SortingDialogArgs by navArgs()

    val viewModel: SortingDialogViewModel by viewModels(extrasProducer = {
        defaultViewModelCreationExtras.withCreationCallback<SortingDialogViewModel.Factory> { factory ->
            factory.create(args.folderId)
        }
    })
    private val disposable = CompositeDisposable()

    private var checkedFileTagIds = mutableSetOf<Long>()
    private var checkedFolderTagIds = mutableSetOf<Long>()

    private val tagsState = mutableStateListOf<Tag>()
    private val fileTagsState = mutableStateListOf<Tag>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(AddTagFragment.REQUEST_KEY) { _, result ->
            val newTags = BundleCompat.getParcelableArrayList(result, AddTagFragment.TAGS_KEY, Tag::class.java) ?: return@setFragmentResultListener
            tagsState.clear()
            tagsState.addAll(newTags)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogFileOrderByBinding.inflate(inflater, container, false)

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
                val tags = FilterTags(checkedFileTagIds.toList(), checkedFolderTagIds.toList())
                if(selections.field != checkedField || selections.sort != checkedOrder || selections.tags != tags) {
                    val formValues = SortDialogChecked(checkedField, checkedOrder, tags)
                    setFragmentResult(RESULT_KEY, bundleOf(SORT_KEY to formValues))
                }
            }

            // handle tags
            val folderId = args.folderId
            val folderList = folderId == FOLDER_TAGS
            val isSingleFolder = !folderList && folderId != GET_ALL_TAGS

            disposable.add(viewModel.tags
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { tags ->
                        val fileTags = mutableListOf<Tag>()
                        val folderTags = mutableListOf<Tag>()
                        tags.forEach { tag ->
                            if (tag.type == TagType.File) {
                                fileTags.add(tag)
                            } else {
                                folderTags.add(tag)
                            }
                        }
                        tagsState.clear()
                        tagsState.addAll(folderTags)
                        fileTagsState.clear()
                        fileTagsState.addAll(fileTags)
                    },
                    Timber::e
                )
            )

            composeRoot.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    val selectedTags = rememberSaveable { args.selections.tags.folderTagIds.toMutableStateList() }
                    val selectedFileTags = rememberSaveable { args.selections.tags.fileTagIds.toMutableStateList() }

                    clearTagsButton.setOnClickListener {
                        checkedFileTagIds.clear()
                        checkedFolderTagIds.clear()
                        selectedTags.clear()
                        selectedFileTags.clear()
                        clearTagsButton.isVisible = false
                    }
                    checkedFileTagIds.addAll(selectedFileTags.toList())
                    checkedFolderTagIds.addAll(selectedTags.toList())
                    updateClearButton()

                    AppTheme {
                        Column {
                            val folderGroupVisible = tagsState.isNotEmpty() || isSingleFolder

                            TagGroup(
                                fileTagsState.toList(),
                                selectedFileTags.toList(),
                                canAddTag = false,
                                showCount = true,
                                hasBottomInset = !folderGroupVisible,
                                onCheckedStateChange = { id, isChecked ->
                                    if (isChecked) {
                                        checkedFileTagIds.add(id)
                                        selectedFileTags.add(id)
                                    } else {
                                        checkedFileTagIds.remove(id)
                                        selectedFileTags.remove(id)
                                    }
                                    updateClearButton()
                                }
                            )

                            if (fileTagsState.isNotEmpty() && folderGroupVisible)
                                HorizontalDivider()

                            TagGroup(
                                tagsState.toList(),
                                selectedTags.toList(),
                                isSingleFolder,
                                folderList,
                                folderGroupVisible,
                                onAddClicked = {
                                    val action = NavGraphDirections.globalToAddTagFragment(longArrayOf(folderId), tagsState.toTypedArray(), TagType.Folder)
                                    findNavController().navigate(action)
                                },
                                onCheckedStateChange = { id, isChecked ->
                                    if (isChecked) {
                                        checkedFolderTagIds.add(id)
                                        selectedTags.add(id)
                                    } else {
                                        checkedFolderTagIds.remove(id)
                                        selectedTags.remove(id)
                                    }
                                    updateClearButton()
                                }
                            )
                        }

                    }
                }
            }
        }


        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        object : BottomSheetDialog(requireContext(), theme) {
            override fun onAttachedToWindow() {
                super.onAttachedToWindow()

                ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
                    // Remove the default padding added to a material sheet, the padding is handled by the tag group composables
                    findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let {
                        it.post {
                            it.updatePadding(bottom = 0)
                        }
                    }
                    insets
                }
            }
        }

    fun updateClearButton() {
        binding.clearTagsButton.isVisible = checkedFileTagIds.isNotEmpty() || checkedFolderTagIds.isNotEmpty()
    }

    override fun onDestroyView() {
        disposable.clear()
        _binding = null
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
    val tags: FilterTags = FilterTags(),
): Parcelable {

    companion object{
        val DEFAULT_FILE = SortDialogChecked(SortField.DEFAULT, Order.DEFAULT)
        val DEFAULT_FOLDER = SortDialogChecked(SortField.DEFAULT_FOLDER, Order.DEFAULT)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagGroup(
    tags: List<Tag>,
    selectedTags: List<Long>,
    canAddTag: Boolean,
    showCount: Boolean = false,
    hasBottomInset: Boolean = false,
    onAddClicked: () -> Unit = {},
    onSelectionChanged: (List<Long>) -> Unit = {},
    onCheckedStateChange: (id: Long, isChecked: Boolean) -> Unit = {_, _ -> },
) {
    val selectedTags = remember(selectedTags) { selectedTags.toMutableStateList() }

    // Add extra item if the add button is visible
    val itemCount = tags.size + if (canAddTag) 1 else 0
    @Suppress("DEPRECATION")
    ContextualFlowRow(
        itemCount,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .heightIn(0.dp, 200.dp)
            .nestedScroll(rememberNestedScrollInteropConnection())
            .verticalScroll(rememberScrollState()),
    ) { i ->
        if (canAddTag && i == 0) {
            val icon = tags.isNotEmpty()
            AssistChip(
                onClick = onAddClicked,
                label = {
                    val text = if (icon) "" else stringResource(R.string.add_tag)
                    Text(text)
                },
                leadingIcon = {
                    Icon(
                        painterResource(if (icon) R.drawable.ic_baseline_edit_24 else R.drawable.ic_baseline_add_24),
                        contentDescription = "Add tag",
                        Modifier.size(AssistChipDefaults.IconSize)
                    )
                },
                modifier = if (icon)
                    Modifier.padding(vertical = 8.dp) // This value is the default padding for AssistChip
                        .size(AssistChipDefaults.Height)
                else Modifier
            )
            return@ContextualFlowRow
        }

        // Handle index offset if the add button exists
        val tag = tags[i - if (canAddTag) 1 else 0]
        val isSelected = selectedTags.contains(tag.id)
        if (canAddTag) {
            SuggestionChip(
                onClick = {},
                label = { Text(tag.name) }
            )
        } else {
            FilterChip(
                onClick = {
                    if (isSelected) {
                        selectedTags.remove(tag.id)
                    } else {
                        selectedTags.add(tag.id)
                    }
                    onSelectionChanged(selectedTags.toList())
                    onCheckedStateChange(tag.id, !isSelected)
                },
                label = {
                    val text = if (showCount) "${tag.name} (${tag.count})" else tag.name
                    Text(text)
                },
                selected = isSelected,
            )
        }

        if (hasBottomInset && i == itemCount - 1)
            Spacer(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
    }
}

@Preview
@Composable
fun TagGroupPreview() {
    val tags = listOf(Tag("Test", Date(), 0), Tag("Test 2", Date(), 1))
    Surface {
        TagGroup(tags, listOf(0), true, onAddClicked = {} , onSelectionChanged = {})
    }
}

@Preview
@Composable
fun TagGroupEmptyPreview() {
    Surface {
        TagGroup(emptyList(), emptyList(), true, onAddClicked = {} , onSelectionChanged = {})
    }
}
