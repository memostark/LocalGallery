package com.guillermonegrete.gallery.files

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.guillermonegrete.gallery.MyApplication
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.files.details.FileDetailsFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FilesListFragment: Fragment() {

    private lateinit var loadingIcon: ProgressBar
    private lateinit var messageContainer: View
    private lateinit var filesList: RecyclerView

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<FilesViewModel> { viewModelFactory }

    private val disposable = CompositeDisposable()

    override fun onAttach(context: Context) {
        (context.applicationContext as MyApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_files_list, container, false)

        val folder = arguments?.getString(FOLDER_KEY) ?: ""
        val folderName: TextView = root.findViewById(R.id.folder_name_text)
        folderName.text = folder

        filesList = root.findViewById(R.id.files_list)
        filesList.layoutManager = LinearLayoutManager(context)

        loadingIcon = root.findViewById(R.id.files_progress_bar)

        messageContainer = root.findViewById(R.id.files_message_container)

        bindViewModel(folder)

        return root
    }

    override fun onStop() {
        disposable.clear()
        super.onStop()
    }

    private fun bindViewModel(folder: String){
        disposable.add(viewModel.loadFiles(folder)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { filesList.adapter = FilesAdapter(it, viewModel) },
                { error -> println("Error loading files: ${error.message}") }
            )
        )

        disposable.add(viewModel.loadingIndicator
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ loadingIcon.visibility = if(it) View.VISIBLE else View.GONE }
        )

        disposable.add(viewModel.networkError
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ messageContainer.visibility = if(it) View.VISIBLE else View.GONE })

        disposable.add(viewModel.openFolder
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ openFileDetails(it) }
        )
    }

    private fun openFileDetails(index: Int){
        val bundle = Bundle()
        bundle.putInt(FileDetailsFragment.FILE_INDEX_KEY, index)
        findNavController().navigate(R.id.fileDetailsFragment, bundle)
    }



    companion object{
        const val FOLDER_KEY = "folder"
    }
}