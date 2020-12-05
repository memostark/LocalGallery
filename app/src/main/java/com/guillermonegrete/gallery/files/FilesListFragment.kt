package com.guillermonegrete.gallery.files

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.*
import com.guillermonegrete.gallery.MyApplication
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.databinding.FragmentFilesListBinding
import com.guillermonegrete.gallery.files.details.FileDetailsFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FilesListFragment: Fragment(R.layout.fragment_files_list) {

    private  var _binding: FragmentFilesListBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<FilesViewModel> { viewModelFactory }

    private val disposable = CompositeDisposable()

    override fun onAttach(context: Context) {
        (context.applicationContext as MyApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFilesListBinding.bind(view)

        with(binding){
            // Set up toolbar
            (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)

            val layoutManager = FlexboxLayoutManager(context).apply {
                flexWrap = FlexWrap.WRAP
                flexDirection = FlexDirection.ROW
                alignItems = AlignItems.STRETCH
            }

            filesList.layoutManager = layoutManager
        }

        val folder = arguments?.getString(FOLDER_KEY) ?: ""
        bindViewModel(folder)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        setFileClickEvent()
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
                { binding.filesList.adapter = FilesAdapter(folder, it, viewModel) },
                { error -> println("Error loading files: ${error.message}") }
            )
        )

        disposable.add(viewModel.loadingIndicator
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ binding.loadingIcon.visibility = if(it) View.VISIBLE else View.GONE }
        )

        disposable.add(viewModel.networkError
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ binding.filesMessageContainer.visibility = if(it) View.VISIBLE else View.GONE })
    }

    private fun setFileClickEvent(){
        disposable.add(viewModel.openDetails
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