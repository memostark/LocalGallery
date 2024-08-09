package com.guillermonegrete.gallery.folders

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.common.Order
import com.guillermonegrete.gallery.common.SortDialogChecked
import com.guillermonegrete.gallery.common.SortingDialog
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.source.SettingsRepository
import com.guillermonegrete.gallery.databinding.FragmentFoldersListBinding
import com.guillermonegrete.gallery.files.SortField
import com.guillermonegrete.gallery.files.details.FileDetailsFragment
import com.guillermonegrete.gallery.folders.models.FolderUI
import com.guillermonegrete.gallery.servers.ServersFragment
import com.jakewharton.rxbinding4.appcompat.queryTextChanges
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

    private val viewModel: FoldersViewModel by viewModels()

    @Inject lateinit var preferences: SettingsRepository

    private lateinit var checkedField: SortField
    private lateinit var checkedOrder: Order

    private val disposable = CompositeDisposable()

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
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        adapter = FolderAdapter()
        val sorting = preferences.getFolderSort()
        checkedField = sorting.field
        checkedOrder = sorting.sort
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                        val action = FoldersListFragmentDirections.actionFoldersToSortingDialog(options, SortDialogChecked(checkedField, checkedOrder))
                        findNavController().navigate(action)
                        setFragmentResultListener(SortingDialog.RESULT_KEY) { _, bundle ->
                            // We use a String here, but any type that can be put in a Bundle is supported
                            val result: SortDialogChecked = bundle.getParcelable(SortingDialog.SORT_KEY) ?: return@setFragmentResultListener
                            checkedField = result.field
                            checkedOrder = result.sort

                            viewModel.updateSort(checkedField.field, checkedOrder.oder)
                        }
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
                    else -> false
                }
            }


            val layoutManager = GridLayoutManager(requireContext(), 2)
            layoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup(){
                override fun getSpanSize(position: Int) = if(position == 0) 2 else 1
            }
            foldersList.layoutManager = layoutManager

            messageIcon.setOnClickListener { viewModel.refresh() }
        }

        setViewModel()
        loadFoldersData()
    }

    override fun onDestroyView() {
        disposable.clear()
        adapter.removeLoadStateListener(loadListener)
        binding.foldersList.adapter = null
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

        disposable.add(adapter.clickSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    hideKeyboard()
                    viewModel.folderSelection = it.position
                    openFileFragment(Folder(it.item))
                },
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
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
        searchView.maxWidth = Int.MAX_VALUE

        disposable.add(
            searchView.queryTextChanges()
                .skip(1) // ignore the first event that happens automatically when subscribing it cause an undesired list refresh
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribe(
                    { viewModel.updateFilter(it) },
                    { Timber.e(it, "Error when querying search view") }
                )
        )

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            // If keyboard is closed (hasFocus is false) and no text in search
            if(!hasFocus && searchView.query.isNullOrBlank()) searchView.isIconified = true
        }

        view?.isFocusableInTouchMode = true
        view?.setOnKeyListener { _, keyCode, _ ->
            return@setOnKeyListener if(keyCode == KeyEvent.KEYCODE_BACK && !searchView.isIconified) {
                // If search view is open, close it instead of closing the app
                searchView.isIconified = true
                true
            } else false
        }
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
}
