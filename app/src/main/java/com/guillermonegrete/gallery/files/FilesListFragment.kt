package com.guillermonegrete.gallery.files

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.ViewModelFactory
import com.guillermonegrete.gallery.data.source.DefaultFilesRepository
import com.guillermonegrete.gallery.data.source.DefaultSettingsRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class FilesListFragment: Fragment() {

    private lateinit var filesList: RecyclerView

    private lateinit var viewModel: FilesViewModel

    private val disposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_files_list, container, false)

        val folder = arguments?.getString(FOLDER_KEY) ?: ""
        val folderName: TextView = root.findViewById(R.id.folder_name_text)
        folderName.text = folder

        val factory = ViewModelFactory(DefaultSettingsRepository(requireContext()), DefaultFilesRepository())
        viewModel = ViewModelProvider(this, factory).get(FilesViewModel::class.java)

        filesList = root.findViewById(R.id.files_list)
        filesList.layoutManager = LinearLayoutManager(context)

        bindViewModel(folder)

        return root
    }

    override fun onStop() {
        disposable.clear()
        super.onStop()
    }

    private fun bindViewModel(folder: String){
        disposable.add(viewModel
            .loadFiles(folder)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { filesList.adapter = FilesAdapter(it) },
                { error -> println("Error loading files: ${error.message}") }
            )
        )
    }



    companion object{
        const val FOLDER_KEY = "folder"
    }
}