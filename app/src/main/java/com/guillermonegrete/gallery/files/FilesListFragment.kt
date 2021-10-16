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
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.flatMap
import androidx.paging.map
import androidx.recyclerview.widget.GridLayoutManager
import com.guillermonegrete.gallery.MyApplication
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.ImageFile
import com.guillermonegrete.gallery.data.VideoFile
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

    private lateinit var adapter: FilesAdapter

    override fun onAttach(context: Context) {
        (context.applicationContext as MyApplication).appComponent.inject(this)
        super.onAttach(context)
        adapter = FilesAdapter(viewModel)
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
        disposable.clear()
        adapter.removeLoadStateListener(loadListener)
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        setFileClickEvent()
    }

    private fun bindViewModel(folder: String){
        adapter.addLoadStateListener(loadListener)
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
                // Hacky way used to find out how many items are in the list
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
                            // Ratio in range, add row
                            normalizeHeights(tempSizes, width / arSum)
                            arSum = 0f
                            val files = updateSizes(tempList, tempSizes)
                            tempList.clear()
                            tempSizes.clear()
                            files
                        }
                        arSum > arMax -> {
                            // Ratio too big, remove last and add the rest as a row
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
                            // Last item, add row with remaining
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
                { adapter.submitData(lifecycle, it) },
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
        var totalWidth = 0
        for (temp in subList) {
            val width = (height * getAspectRatio(temp)).toInt()
            totalWidth += width
            temp.width = width
            temp.height = height.toInt()
        }

        // Sometimes the total width is off by a couple of pixels, e.g. (screen: 720, total: 718)
        // Add the remaining to compensate
        val remaining = getScreenWidth() - totalWidth
        if(remaining > 0) subList.last().width += remaining
    }

    @Synchronized
    private fun getAspectRatio(dim: Size): Float {
        return 1.0f * dim.width / dim.height
    }

    private fun updateSizes(files: List<File>, sizes: List<Size>): List<File>{
        return sizes.mapIndexed { index, newSize ->
            when(val oldFile = files[index]){
                is ImageFile -> ImageFile(oldFile.name, newSize.width, newSize.height)
                is VideoFile -> VideoFile(oldFile.name, newSize.width, newSize.height, oldFile.duration)
            }
        }
    }

    private val loadListener  = { loadStates: CombinedLoadStates ->
        binding.loadingIcon.isVisible = loadStates.refresh is LoadState.Loading
        binding.filesMessageContainer.isVisible = loadStates.refresh is LoadState.Error
    }

    data class Size(var width: Int, var height: Int)

    private fun Fragment.getScreenWidth(): Int{
        val dm = DisplayMetrics()
        this.requireActivity().windowManager.defaultDisplay.getMetrics(dm)
        return dm.widthPixels
    }
}