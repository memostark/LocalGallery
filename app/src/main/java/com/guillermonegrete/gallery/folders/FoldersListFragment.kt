package com.guillermonegrete.gallery.folders

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.BundleCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.guillermonegrete.gallery.NavGraphDirections
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.common.Order
import com.guillermonegrete.gallery.common.SortDialogChecked
import com.guillermonegrete.gallery.common.SortingDialog
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.TagType
import com.guillermonegrete.gallery.data.source.SettingsRepository
import com.guillermonegrete.gallery.data.source.remote.FilterTags
import com.guillermonegrete.gallery.databinding.FragmentFoldersListBinding
import com.guillermonegrete.gallery.files.DragSelectTouchListener
import com.guillermonegrete.gallery.files.SortField
import com.guillermonegrete.gallery.files.details.AddTagFragment
import com.guillermonegrete.gallery.files.details.FileDetailsFragment
import com.guillermonegrete.gallery.folders.models.FolderUI
import com.guillermonegrete.gallery.servers.ServersFragment
import com.guillermonegrete.gallery.ui.theme.AppTheme
import com.jakewharton.rxbinding4.appcompat.queryTextChangeEvents
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class FoldersListFragment: Fragment(R.layout.fragment_folders_list){

    private  var _binding: FragmentFoldersListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FolderAdapter
    private var actionMode: ActionMode? = null
    private var dragSelectTouchListener: DragSelectTouchListener? = null

    private val viewModel: FoldersViewModel by viewModels()

    @Inject lateinit var preferences: SettingsRepository

    private lateinit var checkedField: SortField
    private lateinit var checkedOrder: Order
    private var tagIds = emptyList<Long>()

    private val disposable = CompositeDisposable()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        adapter = FolderAdapter()
        val sorting = preferences.getFolderSort()
        checkedField = sorting.field
        checkedOrder = sorting.sort
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(ServersFragment.REQUEST_KEY) { _, bundle ->
            val ip = bundle.getString(ServersFragment.IP_KEY) ?: return@setFragmentResultListener
            viewModel.updateServerUrl(ip)
        }

        setFragmentResultListener(FileDetailsFragment.FOLDER_UPDATE_KEY) { _, bundle ->
            val newCoverUrl = bundle.getString(FileDetailsFragment.COVER_URL_KEY) ?: return@setFragmentResultListener
            updateFolderItem(newCoverUrl)
        }

        setFragmentResultListener(AddTagFragment.SELECT_TAG_REQUEST_KEY) { _, bundle ->
            val ids = bundle.getLongArray(AddTagFragment.UPDATED_ITEMS_IDS_KEY) ?: return@setFragmentResultListener
            val act = activity
            val message = resources.getQuantityString(
                R.plurals.folders_updated_text,
                ids.size,
                ids.size
            )
            if (act is MainActivity)
                binding.root.post { act.showSnackBar(message) }
        }

        if (savedInstanceState != null) {
            val actionItems = savedInstanceState.getIntegerArrayList(ACTION_MODE_ITEMS_KEY)
            if (actionItems != null) {
                adapter.selectedItems.addAll(actionItems)
                actionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(actionModeCallback)
                updateActionModeBar(actionItems.size)
            }
        }

        tagIds = viewModel.getTags()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener(SortingDialog.RESULT_KEY) { _, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            val result = BundleCompat.getParcelable(bundle, SortingDialog.SORT_KEY, SortDialogChecked::class.java) ?: return@setFragmentResultListener
            checkedField = result.field
            checkedOrder = result.sort
            tagIds = result.tags.folderTagIds

            val filter = FoldersViewModel.ListFilter(checkedField.field, checkedOrder.oder)
            viewModel.updateSort(filter)
            viewModel.setTag(tagIds)
        }

        _binding = FragmentFoldersListBinding.bind(view)

        with(binding){
            // Set up toolbar
            toolbar.setTitle(R.string.app_name)
            toolbar.inflateMenu(R.menu.menu_folders_list_frag)

            val nightModeItem = toolbar.menu.findItem(R.id.night_mode_menu_item)
            nightModeItem.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

            val autoplayItem = toolbar.menu.findItem(R.id.autoplay_menu_item)
            autoplayItem.isChecked = preferences.getAutoPlayMode()

            setSearchViewConfig(toolbar.menu)
            toolbar.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.set_server_menu_item -> {
                        loadDialogData()
                        true
                    }
                    R.id.action_sort -> {
                        val options = SortField.toDisplayArray(listOf(SortField.NAME, SortField.COUNT))
                        val selections = SortDialogChecked(checkedField, checkedOrder,
                            FilterTags(folderTagIds = tagIds)
                        )
                        val action = NavGraphDirections.globalToSortingDialog(selections, options)
                        findNavController().navigate(action)
                        true
                    }
                    R.id.night_mode_menu_item -> {
                        nightModeItem.isChecked = !nightModeItem.isChecked
                        val mode = if (nightModeItem.isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                        preferences.setNightMode(mode)
                        AppCompatDelegate.setDefaultNightMode(mode)
                        true
                    }
                    R.id.autoplay_menu_item -> {
                        autoplayItem.isChecked = !autoplayItem.isChecked
                        viewModel.setAutoplayVideo(autoplayItem.isChecked)
                        true
                    }
                    R.id.select_folders_menu_item -> {
                        actionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(actionModeCallback)
                        true
                    }
                    else -> false
                }
            }


            // Set up list
            val layoutManager = GridLayoutManager(requireContext(), 2)
            layoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup(){
                override fun getSpanSize(position: Int) = if(position == 0) 2 else 1
            }
            foldersList.layoutManager = layoutManager

            val selectListener = object: DragSelectTouchListener.OnAdvancedDragSelectListener {
                override fun onSelectionStarted(start: Int) {
                    actionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(actionModeCallback)
                    updateActionModeBar(1)
                    adapter.setSelected(start)
                }

                override fun onSelectionFinished(end: Int) {}

                override fun onSelectChange(start: Int, end: Int, isSelected: Boolean) {
                    if (isSelected) adapter.setSelected(start, end) else adapter.setUnselected(start, end)
                    updateActionModeBar(adapter.selectedItems.size)
                }
            }
            val listener = DragSelectTouchListener().withSelectListener(selectListener)
            foldersList.addOnItemTouchListener(listener)
            dragSelectTouchListener = listener


            ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, viewInsets ->
                val insets = viewInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = insets.top }
                foldersList.updatePadding(left = insets.left, right = insets.right, bottom = insets.bottom)
                WindowInsetsCompat.CONSUMED
            }

            messageIcon.setOnClickListener { viewModel.refresh() }

            composeRoot.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                setContent {
                    AppTheme {
                        viewModel.showBottomSheet?.let { folderId ->
                            FolderItemMenu(
                                {
                                    // get the folder tags
                                    val action = NavGraphDirections.globalToAddTagFragment(longArrayOf(folderId), arrayOf(), TagType.Folder)
                                    findNavController().navigate(action)
                                    viewModel.removeFolderMenu()
                                },
                                { viewModel.removeFolderMenu() }
                            )
                        }
                    }
                }
            }
        }

        setViewModel()
        loadFoldersData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val list = if (adapter.multiSelect) ArrayList(adapter.selectedItems) else null
        outState.putIntegerArrayList(ACTION_MODE_ITEMS_KEY, list)

        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        disposable.clear()
        adapter.removeLoadStateListener(loadListener)
        binding.foldersList.adapter = null
        dragSelectTouchListener = null
        _binding = null
        super.onDestroyView()
    }

    private fun setViewModel() {
        viewModel.apply {

            disposable.add(urlAvailable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                    setMessageContainer(!it, getString(R.string.no_address_message), R.drawable.ic_settings_input_antenna_black_24dp)
                }
            )
        }
    }

    private fun loadFoldersData(){
        adapter.addLoadStateListener(loadListener)
        binding.foldersList.adapter = adapter
        disposable.add(viewModel.pagedFolders
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { adapter.submitData(lifecycle, it) },
                { error -> Timber.e(error, "Error loading folders") }
            )
        )

        disposable.addAll(adapter.clickSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    hideKeyboard()
                    viewModel.folderSelection = it.position
                    openFileFragment(Folder(it.item))
                },
                { error -> Timber.e(error, "Error on clicking folder") }
            ),
            adapter.longPressSubject
                .subscribe(
                    { dragSelectTouchListener?.startDragSelection(it) },
                    Timber::e,
                ),
            adapter.itemSelectedSubject
                .subscribe(
                    { updateActionModeBar(adapter.selectedItems.size) },
                    { error -> Timber.e(error, "Error on clicking folder") }
                )
        )

        viewModel.getFolders()
    }

    private fun loadDialogData(){
        disposable.add(viewModel.getDialogData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { openServerFragment(it) },
                { error -> Timber.e(error, "Unable to log dialog data") }
            )
        )
    }

    private fun openFileFragment(folder: Folder){
        val action = FoldersListFragmentDirections.openFolder(folder)
        findNavController().navigate(action)
    }

    private fun openServerFragment(presetIp: String){
        val bundle = Bundle()
        bundle.putString(ServersFragment.IP_KEY, presetIp)
        findNavController().navigate(R.id.servers_fragment_dest, bundle)
    }

    private fun setSearchViewConfig(menu: Menu){
        val searchManager = context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
        searchView.maxWidth = Int.MAX_VALUE
        searchView.imeOptions = searchView.imeOptions or EditorInfo.IME_FLAG_NO_EXTRACT_UI

        disposable.add(
            searchView.queryTextChangeEvents()
                .skip(1) // ignore the first event that happens automatically when subscribing it cause an undesired list refresh
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribe(
                    {
                        if (!it.isSubmitted) {
                            viewModel.updateFilter(it.queryText)
                        } else {
                            hideKeyboard()
                        }
                    },
                    { Timber.e(it, "Error when querying search view") }
                )
        )

        val searchBackCallback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, false) {
            searchItem.collapseActionView()
        }

        searchItem.setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                searchBackCallback.isEnabled = true
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                searchBackCallback.isEnabled = false
                return true
            }
        })
    }

    private fun setMessageContainer(visible: Boolean, message: String, icon: Int){
        with(binding){
            if(visible){
                this.message.text = message
                messageIcon.setImageResource(icon)
            }
            foldersList.isGone = visible
            messageContainer.isVisible = visible
        }
    }

    private fun hideKeyboard() {
        val context = activity ?: return
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val focusedView = context.currentFocus ?: return
        inputMethodManager.hideSoftInputFromWindow(focusedView.windowToken, 0)
    }

    private fun updateFolderItem(newCoverUrl: String){
        val index = viewModel.folderSelection
        val folderUi = adapter.snapshot().items[index]
        if(folderUi is FolderUI.Model) folderUi.coverUrl = newCoverUrl
    }

    private fun updateActionModeBar(items: Int) {
        actionMode?.title = "($items)"
        val folderMenuItem = actionMode?.menu?.findItem(R.id.show_item_menu)
        if (folderMenuItem != null) {
            folderMenuItem.isVisible = items == 1
        }
    }

    private val loadListener  = { loadStates: CombinedLoadStates ->
        val state = loadStates.refresh
        binding.loadingBar.isVisible = state is LoadState.Loading
        binding.messageContainer.isVisible = state is LoadState.Error
        if(state is LoadState.Error) {
            Timber.e(state.error, "Load state listener error")
            setMessageContainer(true, getString(R.string.error_message), R.drawable.ic_refresh_black_24dp)
        } else if(state is LoadState.NotLoading){
            setMessageContainer(adapter.itemCount < 1, getString(R.string.folder_empty_message), R.drawable.ic_folder_open_black_24dp)
        }
    }

    private val actionModeCallback = object: ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            adapter.setSelectionMode(true)
            mode.menuInflater.inflate(R.menu.folders_action_mode_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.add_tag -> {
                    val action = NavGraphDirections.globalToAddTagFragment(adapter.selectedIds.toLongArray(), emptyArray(), TagType.Folder)
                    findNavController().navigate(action)
                    true
                }
                R.id.show_item_menu -> {
                    val item = adapter.selectedIds.firstOrNull() ?: return false
                    viewModel.setFolderMenu(item)
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            adapter.setSelectionMode(false)
            actionMode = null
        }
    }

    companion object {
        const val ACTION_MODE_ITEMS_KEY = "action_mode_items"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderItemMenu(
    onItemClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(painterResource(R.drawable.ic_baseline_edit_24), contentDescription = "Edit tags")
                },
                text = { Text(stringResource(R.string.edit_tags)) },
                onClick = onItemClick
            )
        }
    }
}
