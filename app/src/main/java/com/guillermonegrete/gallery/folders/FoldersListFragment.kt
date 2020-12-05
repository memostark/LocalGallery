package com.guillermonegrete.gallery.folders

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.guillermonegrete.gallery.MyApplication
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.databinding.FragmentFoldersListBinding
import com.guillermonegrete.gallery.files.FilesListFragment
import com.guillermonegrete.gallery.servers.ServersFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FoldersListFragment: Fragment(R.layout.fragment_folders_list){

    private val TAG = FoldersListFragment::class.java.simpleName

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
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFoldersListBinding.bind(view)

        with(binding){
            // Set up toolbar
            (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)


            val layoutManager = GridLayoutManager(requireContext(), 2)
            layoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup(){
                override fun getSpanSize(position: Int) = if(position == 0) 2 else 1
            }
            foldersList.layoutManager = layoutManager

            messageIcon.setOnClickListener { loadFoldersData() }
        }


        // Listen to updates from Servers Fragment
        // TODO compare this method with the new fragment communication API
        val stateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        stateHandle?.getLiveData<String>(ServersFragment.IP_KEY)?.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, "New ip: $it", Toast.LENGTH_SHORT).show()
            viewModel.updateServerUrl(it)
            loadFoldersData()
        })
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        setViewModel()
    }

    override fun onStop() {
        disposable.clear()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_folders_list_frag, menu)
        setSearchViewConfig(menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.set_server_menu_item -> {
                loadDialogData()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setViewModel() {
        viewModel.apply {

            disposable.add(loadingIndicator
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {binding.loadingBar.visibility = if(it) View.VISIBLE else View.GONE}
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
                .subscribe{ openFileFragment(it) }
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
        loadFoldersData()
    }

    private fun loadFoldersData(){
        disposable.add(viewModel.getFolders()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    adapter = FolderAdapter(it, viewModel)
                    binding.foldersList.adapter = adapter
                },
                {error -> println("Error loading folders: ${error.message}")}
            )
        )
    }

    private fun loadDialogData(){
        disposable.add(viewModel.getDialogData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { openServerFragment(it) },
                { error -> Log.e(TAG,"Unable to log dialog data $error") }
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
                adapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })
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

}