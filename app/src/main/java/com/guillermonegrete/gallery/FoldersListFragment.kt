package com.guillermonegrete.gallery

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.guillermonegrete.gallery.data.source.DefaultFilesRepository
import com.guillermonegrete.gallery.data.source.DefaultSettingsRepository
import com.guillermonegrete.gallery.files.FilesListFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class FoldersListFragment: Fragment(){

    private lateinit var loadingIcon: ProgressBar
    private lateinit var folderListContainer: View
    private lateinit var messageContainer: View
    private lateinit var messageIcon: ImageView
    private lateinit var messageText: TextView
    private lateinit var folderList: RecyclerView

    private lateinit var viewModel: FoldersViewModel

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_folders_list, container, false)

        val rootFolderName: TextView = root.findViewById(R.id.textView_root_folder)
        rootFolderName.text = "Root folder name"

        folderList = root.findViewById(R.id.folders_list)
        folderList.layoutManager = GridLayoutManager(requireContext(), 2)

        loadingIcon = root.findViewById(R.id.folders_progress_bar)
        folderListContainer = root.findViewById(R.id.folders_linear_layout)

        messageContainer = root.findViewById(R.id.folders_message_container)
        messageIcon = root.findViewById(R.id.foldersMessageIcon)
        messageText = root.findViewById(R.id.foldersMessageMain)

        val factory = ViewModelFactory(DefaultSettingsRepository(requireContext()), DefaultFilesRepository())
        viewModel = ViewModelProvider(this, factory).get(FoldersViewModel::class.java)

        return root
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

    private fun showServerDialog(presetData: String) {
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_set_server_address, null)
        val addressText: EditText = dialogLayout.findViewById(R.id.server_address_edit)
        addressText.setText(presetData)
        addressText.setSelection(presetData.length)

        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Set server address")
            .setView(dialogLayout)
            .setPositiveButton(R.string.ok) { _, _ ->
                viewModel.updateServerUrl(addressText.text.toString())
                loadFoldersData()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->}

        builder.create().show()

    }

    private fun setViewModel() {
        viewModel.apply {

            disposable.add(loadingIndicator
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {loadingIcon.visibility = if(it) View.VISIBLE else View.GONE}
            )

            disposable.add(urlAvailable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                    if(!it){
                        messageText.text = resources.getString(R.string.no_address_message)
                        messageIcon.setImageResource(R.drawable.ic_settings_input_antenna_black_24dp)
                    }
                    folderListContainer.visibility = if(it) View.VISIBLE else View.GONE
                    messageContainer.visibility = if(it) View.GONE else View.VISIBLE
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
                    if(it){
                        messageText.text = resources.getString(R.string.error_message)
                        messageIcon.setImageResource(R.drawable.ic_error_outline_black_24dp)
                    }
                    folderListContainer.visibility = if (it) View.GONE else View.VISIBLE
                    messageContainer.visibility = if (it) View.VISIBLE else View.GONE
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
                {data -> folderList.adapter = FolderAdapter(data, viewModel)},
                {error -> println("Error loading folders: ${error.message}")}
            )
        )
    }

    private fun loadDialogData(){
        disposable.add(viewModel.getDialogData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { showServerDialog(it) },
                { error -> println("Unable to log dialog data $error") }
            )
        )
    }

    private fun openFileFragment(folder: String){
        val bundle = Bundle()
        bundle.putString(FilesListFragment.FOLDER_KEY, folder)
        findNavController().navigate(R.id.files_fragment_dest, bundle)
    }

}