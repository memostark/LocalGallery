package com.guillermonegrete.gallery.files

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.guillermonegrete.gallery.MyApplication
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.databinding.DialogFileOrderByBinding
import com.guillermonegrete.gallery.databinding.FragmentFilesListBinding
import com.guillermonegrete.gallery.files.details.FileDetailsFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class FilesListFragment: Fragment(R.layout.fragment_files_list) {

    private  var _binding: FragmentFilesListBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<FilesViewModel> { viewModelFactory }

    private val disposable = CompositeDisposable()

    private lateinit var adapter: FilesAdapter

    // Default values for the checked items in the sorting dialog
    private var checkedField = R.id.by_name
    private var checkedOrder = R.id.ascending_order

    override fun onAttach(context: Context) {
        (context.applicationContext as MyApplication).appComponent.inject(this)
        super.onAttach(context)
        adapter = FilesAdapter(viewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFilesListBinding.bind(view)
        val folder = arguments?.getString(FOLDER_KEY) ?: ""

        with(binding){
            toolbar.title = folder
            toolbar.inflateMenu(R.menu.files_list_menu)
            toolbar.setOnMenuItemClickListener {
                if(it.itemId == R.id.action_sort){
                    showSortDialog()
                    return@setOnMenuItemClickListener true
                }
                false
            }
        }
        bindViewModel(folder)
    }

    override fun onDestroyView() {
        binding.filesList.adapter = null
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

        viewModel.width = width

        disposable.addAll(viewModel.cachedFileList
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { adapter.submitData(lifecycle, it) },
                { error -> Timber.e(error,"Error loading files") }
            ),
            viewModel.updateRows
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ it.forEach { row ->
                    adapter.snapshot().items[row.pos].width = row.size.width
                    adapter.snapshot().items[row.pos].height = row.size.height
                }}, { error -> Timber.e(error,"Error loading files") }
                )
        )

        viewModel.setFolderName(folder)
    }

    private fun setFileClickEvent(){
        disposable.add(viewModel.openDetails
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::openFileDetails)
        )
    }

    private fun openFileDetails(index: Int){
        val bundle = Bundle()
        bundle.putInt(FileDetailsFragment.FILE_INDEX_KEY, index)
        findNavController().navigate(R.id.fileDetailsFragment, bundle)
    }

    private val fieldIdMap = mapOf(
        R.id.by_name to "filename",
        R.id.by_creation to "creationDate",
        R.id.by_last_modified to "lastModified",
    )

    private val sortIdMap = mapOf(
        R.id.ascending_order to "asc",
        R.id.descending_order to "desc",
    )

    private fun showSortDialog(){
        val dialog = BottomSheetDialog(requireContext())
        val binding = DialogFileOrderByBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        var changed = false

        binding.fieldSort.check(checkedField)
        binding.fieldSort.setOnCheckedChangeListener { _, checkedId ->
            changed = true
            checkedField = checkedId
        }

        binding.orderSort.check(checkedOrder)
        binding.orderSort.setOnCheckedChangeListener { _, checkedId ->
            changed = true
            checkedOrder = checkedId
        }

        binding.doneButton.setOnClickListener {
            if(changed) {
                val field = fieldIdMap[checkedField] ?: "filename"
                val sort = sortIdMap[checkedOrder] ?: "asc"

                // Because ascending is the default order, don't add it to the string filter
                val filter = if(sort == "asc") field else "$field,desc"
                viewModel.setFilter(filter)
                viewModel.setFolderName(arguments?.getString(FOLDER_KEY) ?: "")
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    companion object{
        const val FOLDER_KEY = "folder"
    }

    private val loadListener  = { loadStates: CombinedLoadStates ->
        val state = loadStates.refresh
        binding.loadingIcon.isVisible = state is LoadState.Loading
        binding.filesMessageContainer.isVisible = state is LoadState.Error
        if(state is LoadState.Error) Timber.e(state.error, "Error when loading")
    }

    private fun Fragment.getScreenWidth(): Int{
        val dm = DisplayMetrics()
        this.requireActivity().windowManager.defaultDisplay.getMetrics(dm)
        return dm.widthPixels
    }
}
