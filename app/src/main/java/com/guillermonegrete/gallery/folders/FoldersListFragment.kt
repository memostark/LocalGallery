package com.guillermonegrete.gallery.folders

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.map
import androidx.recyclerview.widget.GridLayoutManager
import com.guillermonegrete.gallery.MyApplication
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.databinding.FragmentFoldersListBinding
import com.guillermonegrete.gallery.files.FilesListFragment
import com.guillermonegrete.gallery.servers.ServersFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class FoldersListFragment: Fragment(R.layout.fragment_folders_list){

    private  var _binding: FragmentFoldersListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FolderAdapter

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<FoldersViewModel> { viewModelFactory }

    private val disposable = CompositeDisposable()

    override fun onAttach(context: Context) {
        (context.applicationContext as MyApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(ServersFragment.REQUEST_KEY) { _, bundle ->
            val ip = bundle.getString(ServersFragment.IP_KEY) ?: return@setFragmentResultListener
            Toast.makeText(context, "New ip: $ip", Toast.LENGTH_SHORT).show()
            viewModel.updateServerUrl(ip)
            loadFoldersData()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFoldersListBinding.bind(view)

        with(binding){
            // Set up toolbar
            toolbar.setTitle(R.string.app_name)
            toolbar.inflateMenu(R.menu.menu_folders_list_frag)
            setSearchViewConfig(toolbar.menu)
            toolbar.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.set_server_menu_item -> {
                        loadDialogData()
                        true
                    }
                    else -> false
                }
            }


            val layoutManager = GridLayoutManager(requireContext(), 2)
            /*layoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup(){
                override fun getSpanSize(position: Int) = if(position == 0) 2 else 1
            }*/
            foldersList.layoutManager = layoutManager

            messageIcon.setOnClickListener { loadFoldersData() }
        }

        loadFoldersData()
    }

    override fun onStart() {
        super.onStart()
        setViewModel()
    }

    override fun onDestroyView() {
        adapter.removeLoadStateListener(loadListener)
        binding.foldersList.adapter = null
        _binding = null
        super.onDestroyView()
    }

    override fun onStop() {
        disposable.clear()
        super.onStop()
    }

    private fun setViewModel() {
        viewModel.apply {

            disposable.add(loadingIndicator
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {binding.loadingBar.isVisible = it }
            )

            disposable.add(urlAvailable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                    setMessageContainer(!it, getString(R.string.no_address_message), R.drawable.ic_settings_input_antenna_black_24dp)
                }
            )

            disposable.add(openFolder
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    hideKeyboard()
                    openFileFragment(it)
                }
            )

            disposable.add(networkError
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    setMessageContainer(
                        it,
                        getString(R.string.error_message),
                        R.drawable.ic_refresh_black_24dp
                    )
                }
            )

            disposable.add(rootFolderEmpty
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    setMessageContainer(
                        it,
                        getString(R.string.folder_empty_message),
                        R.drawable.ic_folder_open_black_24dp
                    )
                }
            )
        }
    }

    private fun loadFoldersData(){
        adapter = FolderAdapter(viewModel)
        adapter.addLoadStateListener(loadListener)
        binding.foldersList.adapter = adapter
        disposable.add(viewModel.getFolders()
            .subscribeOn(Schedulers.io())
            .map { pageData ->
                Timber.d("Paging data: $pageData")
                pageData.map { Timber.d("Got folder: $it"); it }
                pageData
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    adapter.submitData(lifecycle, it)
                },
                { error -> Timber.e(error, "Error loading folders") }
            )
        )
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

    private fun openFileFragment(folder: String){
        val bundle = Bundle()
        bundle.putString(FilesListFragment.FOLDER_KEY, folder)
        findNavController().navigate(R.id.files_fragment_dest, bundle)
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

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
//                adapter.filter.filter(query)
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
//                adapter.filter.filter(newText)
                return false
            }
        })

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
            foldersList.visibility = if (visible) View.GONE else View.VISIBLE
            messageContainer.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    private fun hideKeyboard() {
        val context = activity ?: return
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val focusedView = context.currentFocus ?: return
        inputMethodManager.hideSoftInputFromWindow(focusedView.windowToken, 0)
    }

    private val loadListener  = { loadStates: CombinedLoadStates ->
        val state = loadStates.refresh
//        binding.loadingIcon.isVisible = state is LoadState.Loading
//        binding.filesMessageContainer.isVisible = state is LoadState.Error
        if(state is LoadState.Error) Timber.e(state.error, "Error when loading")
    }
}
