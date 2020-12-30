package com.guillermonegrete.gallery.files

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.flatMap
import androidx.paging.map
import androidx.recyclerview.widget.GridLayoutManager
import com.guillermonegrete.gallery.MyApplication
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.File
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
        val adapter = FilesAdapter(viewModel)
        adapter.addLoadStateListener { loadStates ->
            binding.loadingIcon.isVisible = loadStates.refresh is LoadState.Loading
            binding.filesMessageContainer.isVisible = loadStates.refresh is LoadState.Error

        }
        val list = binding.filesList

        val width = getScreenWidth()

        val layoutManager = GridLayoutManager(context, width)
        list.layoutManager = layoutManager
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val file = adapter.snapshot()[position]
                return file?.width ?: 1
            }
        }
        list.adapter = adapter

        val arMin = 2.0f
        val arMax = 3.0f

        var dataSize = 0

        disposable.add(viewModel.loadPagedFiles(folder)
            .subscribeOn(Schedulers.io())
                // Hacky way used to find out how many
            .map { pagingData ->
                pagingData.map { dataSize++; it }
            }
            .map { pagingData ->
                var arSum = 0f
                val tempList = mutableListOf<File>()
                val tempSizes = mutableListOf<Size>()

                var index = 0

                pagingData.flatMap { file ->
                    val size = Size(file.width, file.height)
                    tempList.add(file)
                    tempSizes.add(size)
                    arSum += getAspectRatio(size)
                    index++

                    when {
                        arSum in arMin..arMax -> {
                            normalizeHeights(tempSizes, width / arSum)
                            arSum = 0f
                            val files = updateSizes(tempList, tempSizes)
                            tempList.clear()
                            tempSizes.clear()
                            files
                        }
                        arSum > arMax -> {
                            val pop = tempSizes.removeLast()
                            val popFile = tempList.removeLast()
                            arSum -= getAspectRatio(pop)
                            normalizeHeights(tempSizes, width / arSum)
                            val files = updateSizes(tempList, tempSizes)
                            tempList.clear()
                            tempSizes.clear()
                            tempSizes.add(pop)
                            tempList.add(popFile)
                            arSum = getAspectRatio(pop)
                            files
                        }
                        index == dataSize -> {
                            dataSize = 0
                            index = 0
                            normalizeHeights(tempSizes, width / arSum)
                            arSum = 0f
                            val files = updateSizes(tempList, tempSizes)
                            tempList.clear()
                            tempSizes.clear()
                            files
                        }
                        else -> emptyList()
                    }
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { adapter.submitData(lifecycle, it); println() },
                { error -> println("Error loading files: ${error.message}") }
            )
        )
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

    @Synchronized
    private fun normalizeHeights(subList: List<Size>, height: Float) {
        for (temp in subList) {
            temp.width = (height * getAspectRatio(temp)).toInt()
            temp.height = height.toInt()
        }
    }

    @Synchronized
    private fun getAspectRatio(dim: Size): Float {
        return 1.0f * dim.width / dim.height
    }

    private fun updateSizes(files: List<File>, sizes: List<Size>): List<File>{
        return sizes.mapIndexed { index, newSize ->
            val oldFile = files[index]
            File(oldFile.name, oldFile.type, newSize.width, newSize.height)
        }
    }

    data class Size(var width: Int, var height: Int)

    private fun Fragment.getScreenWidth(): Int{
        val dm = DisplayMetrics()
        this.requireActivity().windowManager.defaultDisplay.getMetrics(dm)
        return dm.widthPixels
    }
}