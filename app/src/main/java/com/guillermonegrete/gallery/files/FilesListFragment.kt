package com.guillermonegrete.gallery.files

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.os.BundleCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.guillermonegrete.gallery.NavGraphDirections
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.common.Order
import com.guillermonegrete.gallery.common.SortDialogChecked
import com.guillermonegrete.gallery.common.SortingDialog
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.data.source.SettingsRepository
import com.guillermonegrete.gallery.data.source.remote.FilterTags
import com.guillermonegrete.gallery.databinding.FragmentFilesListBinding
import com.guillermonegrete.gallery.files.details.AddTagFragment
import com.guillermonegrete.gallery.folders.MainActivity
import com.guillermonegrete.gallery.utils.hiltNavGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FilesListFragment: Fragment(R.layout.fragment_files_list) {

    private  var _binding: FragmentFilesListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FilesViewModel by hiltNavGraphViewModels {
        if (isAllFiles) R.id.nav_graph else R.id.files_graph
    }
    private val args: FilesListFragmentArgs by navArgs()

    private val disposable = CompositeDisposable()

    private lateinit var adapter: FilesAdapter
    private var actionMode: ActionMode? = null
    private var dragSelectTouchListener: DragSelectTouchListener? = null

    @Inject lateinit var preferences: SettingsRepository

    // Default values for the checked items in the sorting dialog
    private var checkedField = SortField.DEFAULT
    private var checkedOrder = Order.DEFAULT
    private var tagIds = FilterTags()

    private val isAllFiles: Boolean
        get() = args.folder == null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        adapter = FilesAdapter(viewModel)

        // Set in this method instead of onCreateView() to avoid resetting everytime the user navigates back to this fragment (e.g. from details frag)
        if(isAllFiles) {
            viewModel.getFilter()?.let {
                checkedField = SortField.fromField(it.sortType) ?: SortField.DEFAULT
                checkedOrder = Order.fromOrder(it.order)
            }
        } else {
            val sorting = preferences.getFileSort()
            checkedField = sorting.field
            checkedOrder = sorting.sort
        }
        tagIds = viewModel.getTags()
        val newFilter = FilesViewModel.ListFilter(checkedField.field, checkedOrder.oder)
        viewModel.setFilter(newFilter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(SortingDialog.RESULT_KEY) { _, bundle ->
            val result: SortDialogChecked = BundleCompat.getParcelable(bundle, SortingDialog.SORT_KEY, SortDialogChecked::class.java) ?: return@setFragmentResultListener
            checkedField = result.field
            checkedOrder = result.sort
            tagIds = result.tags

            viewModel.setTag(tagIds)
            val newFilter = FilesViewModel.ListFilter(checkedField.field, checkedOrder.oder)
            viewModel.setFilter(newFilter)
            // The sort for All Files is independent from the preference sort
            if (!isAllFiles) preferences.setFileSort(checkedField.field, checkedOrder.oder)
            val folder = BundleCompat.getParcelable(requireArguments(), FOLDER_KEY, Folder::class.java) ?: Folder.NULL_FOLDER
            viewModel.setFolderName(folder)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFilesListBinding.bind(view)
        setTagsUpdateListener()
        restoreState(savedInstanceState)
        val folder = args.folder ?: Folder.NULL_FOLDER

        val id = if(folder == Folder.NULL_FOLDER) SortingDialog.GET_ALL_TAGS else folder.id

        with(binding){
            toolbar.title = folder.name.ifEmpty { getString(R.string.files_toolbar_title) }
            toolbar.inflateMenu(R.menu.files_list_menu)
            toolbar.setOnMenuItemClickListener {
                if(it.itemId == R.id.action_sort){
                    showSortDialog(id)
                    return@setOnMenuItemClickListener true
                }
                false
            }

            filesMessageIcon.setOnClickListener { viewModel.refresh() }
            val selectListener = object: DragSelectTouchListener.OnAdvancedDragSelectListener {
                override fun onSelectionStarted(start: Int) {
                    actionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(actionModeCallback)
                    actionMode?.title = "(1)"
                    adapter.setSelected(start)
                }

                override fun onSelectionFinished(end: Int) {}

                override fun onSelectChange(start: Int, end: Int, isSelected: Boolean) {
                    if (isSelected) adapter.setSelected(start, end) else adapter.setUnselected(start, end)
                    actionMode?.title = "(${adapter.selectedItems.size})"
                }

            }
            ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, viewInsets ->
                val insets = viewInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                binding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = insets.top }
                filesList.updatePadding(left = insets.left, right = insets.right, bottom = insets.bottom)
                WindowInsetsCompat.CONSUMED
            }

            val listener = DragSelectTouchListener().withSelectListener(selectListener)
            filesList.addOnItemTouchListener(listener)
            dragSelectTouchListener = listener
        }
        binding.filesList.post { bindViewModel(folder) }
        setFileClickEvent()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val list = if (adapter.multiSelect) ArrayList(adapter.selectedItems) else null
        outState.putIntegerArrayList(ACTION_MODE_ITEMS_KEY, list)

        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        binding.filesList.adapter = null
        dragSelectTouchListener = null
        _binding = null
        actionMode?.finish()
        disposable.clear()
        adapter.removeLoadStateListener(loadListener)
        super.onDestroyView()
    }

    private fun bindViewModel(folder: Folder){
        adapter.addLoadStateListener(loadListener)
        val list = binding.filesList

        val width = getListWidth()

        val layoutManager = GridLayoutManager(context, width)
        list.layoutManager = layoutManager
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val file = adapter.snapshot()[position]
                return file?.displayWidth ?: 1
            }
        }
        list.adapter = adapter

        disposable.addAll(
            viewModel.cachedFileList
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { adapter.submitData(lifecycle, it) },
                    { error -> Timber.e(error,"Error loading files") }
                ),
            viewModel.updateRows
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ it.forEach { row ->
                    adapter.snapshot().items[row.pos].displayWidth = row.size.width
                    adapter.snapshot().items[row.pos].displayHeight = row.size.height
                }}, { error -> Timber.e(error,"Error updating rows") }
                ),
            viewModel.newFilePos.
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        // Hide the appbar because it pushes the layout which sometimes makes the item
                        // not fully visible
                        binding.appbar.setExpanded(false)
                        layoutManager.scrollToPosition(it)
                    },
                    { error -> Timber.e(error) }
                ),
            adapter.onItemLongPress.subscribe(
                { dragSelectTouchListener?.startDragSelection(it) },
                { Timber.e(it) }
            ),
            adapter.onItemClick.subscribe(
                { actionMode?.title = "(${adapter.selectedItems.size})" },
                { Timber.e(it) }
            )
        )

        if (viewModel.width != width) viewModel.setListWidth(width)
        viewModel.setFolderName(folder)
        // When navigating back to files list, reset the details sheet to hidden
        viewModel.setSheet(false)
    }

    private fun setFileClickEvent(){
        disposable.add(viewModel.openDetails
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::openFileDetails)
        )
    }

    private fun openFileDetails(index: Int){
        val folder = args.folder ?: Folder.NULL_FOLDER
        val action = FilesListFragmentDirections.openFileDetails(index, folder)
        findNavController().navigate(action)
    }

    private fun showSortDialog(id: Long) {
        val action = NavGraphDirections.globalToSortingDialog(SortDialogChecked(checkedField, checkedOrder, tagIds), folderId = id)
        findNavController().navigate(action)
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val actionItems = savedInstanceState.getIntegerArrayList(ACTION_MODE_ITEMS_KEY)
            if (actionItems != null) {
                adapter.selectedItems.addAll(actionItems)
                actionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(actionModeCallback)
                actionMode?.title = "(${actionItems.size})"
            }
        }
    }

    companion object{
        const val FOLDER_KEY = "folder"
        const val ACTION_MODE_ITEMS_KEY = "action_mode_items"
    }

    private val loadListener  = { loadStates: CombinedLoadStates ->
        val state = loadStates.refresh
        binding.loadingIcon.isVisible = state is LoadState.Loading
        binding.filesMessageContainer.isVisible = state is LoadState.Error
        if(state is LoadState.Error) Timber.e(state.error, "Error when loading files")
    }

    private fun getListWidth() = binding.filesList.width

    private val actionModeCallback = object: ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            adapter.setSelectionMode(true)
            mode.menuInflater.inflate(R.menu.files_action_mode_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.add_tag -> {
                    val tags = adapter.getSingleSelectedItem()?.tags?.toTypedArray() ?: emptyArray()
                    val action = FilesListFragmentDirections.actionFilesToAddTagFragment(adapter.selectedIds.toLongArray(), tags)
                    findNavController().navigate(action)
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            adapter.setSelectionMode(false)
        }
    }

    private fun setTagsUpdateListener() {
        setFragmentResultListener(AddTagFragment.SELECT_TAG_REQUEST_KEY) { _, bundle ->
            val newTag = BundleCompat.getParcelable(bundle, AddTagFragment.SELECTED_TAG_KEY, Tag::class.java) ?: return@setFragmentResultListener
            val ids = bundle.getLongArray(AddTagFragment.UPDATED_ITEMS_IDS_KEY) ?: return@setFragmentResultListener

            for (pos in adapter.selectedItems) {
                val file = adapter.snapshot().items[pos]

                // Add if the file was a modified file and if it doesn't contain the tag.
                if(file.id in ids && !file.tags.contains(newTag)){
                    val newTags = file.tags.toMutableList()
                    newTags.add(newTag)
                    file.tags = newTags
                    adapter.notifyItemChanged(pos)
                }
            }
            actionMode?.finish()
            val message = resources.getQuantityString(
                R.plurals.files_updated_text,
                ids.size,
                ids.size
            )
            val act = activity
            binding.root.post {
                if (act is MainActivity) act.showSnackBar(message)
                else Snackbar.make(binding.filesFragmentRoot, message, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
