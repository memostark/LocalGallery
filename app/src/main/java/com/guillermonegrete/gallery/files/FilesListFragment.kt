package com.guillermonegrete.gallery.files

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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
        disposable.add(viewModel.loadFiles(folder)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { setFilesList(folder, it) },
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

    private fun setFilesList(folder: String, files: List<File>){
        val sizes = files.map { Size(it.width, it.height) }
        val dm = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels
        applyAspects(sizes, width)

        val fileListItems = files.mapIndexed { index, file ->
            val size = sizes[index]
            File(file.name, file.type, size.width, size.height)
        }

        val layoutManager = GridLayoutManager(context, width)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if(position == 0) return width // First item is a header
                val spanSize = fileListItems[position - 1].width
                return if (spanSize <= width) spanSize else width
            }
        }
        val list = binding.filesList
        list.layoutManager = layoutManager
        list.adapter = FilesAdapter(folder, fileListItems, viewModel)
    }

    @Synchronized
    private fun applyAspects(
        imageList: List<Size>,
        width: Int,
        ar_min: Float = 2.0f,
        ar_max: Float = 3.0f,
    ) {

        var arSum = 0f
        val tempList = mutableListOf<Size>()
        for (temp in imageList) {
            tempList.add(temp)
            arSum += getAspectRatio(temp)
            if (arSum in ar_min..ar_max) {
                normalizeHeights(tempList, width / arSum)
                arSum = 0f
                tempList.clear()
            } else if (arSum > ar_max) {
                val pop = tempList.removeAt(tempList.size - 1)
                arSum -= getAspectRatio(pop)
                normalizeHeights(tempList, width / arSum)
                tempList.clear()
                tempList.add(pop)
                arSum = getAspectRatio(pop)
            }
        }
        normalizeHeights(tempList, width / arSum)
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

    data class Size(var width: Int, var height: Int)
}