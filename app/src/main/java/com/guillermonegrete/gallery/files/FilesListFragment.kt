package com.guillermonegrete.gallery.files

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.guillermonegrete.gallery.MyApplication
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.common.SortDialogChecked
import com.guillermonegrete.gallery.common.SortingDialog
import com.guillermonegrete.gallery.data.Folder
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
    private var checkedField = FilesSorting.DEFAULT
    private var checkedOrder = SortingDialog.Order.DEFAULT
    private var tagId = SortingDialog.NO_TAG_ID

    override fun onAttach(context: Context) {
        (context.applicationContext as MyApplication).appComponent.inject(this)
        super.onAttach(context)
        adapter = FilesAdapter(viewModel)

        // Reset tags because the ViewModel is shared it may have a previous configuration
        // Reset in this method instead of onCreateView() to avoid resetting everytime the user navigates back to this fragment (e.g. from details frag)
        viewModel.setTag(SortingDialog.NO_TAG_ID)
        viewModel.setFilter("")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFilesListBinding.bind(view)
        val folder: Folder = arguments?.getParcelable(FOLDER_KEY) ?: Folder.NULL_FOLDER

        with(binding){
            toolbar.title = folder.name.ifEmpty { getString(R.string.files_toolbar_title) }
            toolbar.inflateMenu(R.menu.files_list_menu)
            toolbar.setOnMenuItemClickListener {
                if(it.itemId == R.id.action_sort){
                    val id = if(folder == Folder.NULL_FOLDER) SortingDialog.GET_ALL_TAGS else folder.id
                    showSortDialog(id)
                    return@setOnMenuItemClickListener true
                }
                false
            }
        }
        bindViewModel(folder)
        setFileClickEvent()
    }

    override fun onDestroyView() {
        binding.filesList.adapter = null
        _binding = null
        disposable.clear()
        adapter.removeLoadStateListener(loadListener)
        super.onDestroyView()
    }

    private fun bindViewModel(folder: Folder){
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
                }}, { error -> Timber.e(error,"Error updating rows") }
                ),
            viewModel.newFilePos.
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        // Hide the appbar because it pushes the layout which sometimes makes the item
                        // not fully visible
                        binding.appbar.setExpanded(false)
                        layoutManager.scrollToPosition(it)
                    },
                    { error -> Timber.e(error) }
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

    private fun showSortDialog(id: Long) {
        val action = FilesListFragmentDirections.actionFilesToSortingDialog(FilesSorting.toArray(), SortDialogChecked(checkedField.ordinal, checkedOrder, tagId), id)
        findNavController().navigate(action)
        setFragmentResultListener(SortingDialog.RESULT_KEY) { _, bundle ->
            val result: SortDialogChecked = bundle.getParcelable(SortingDialog.SORT_KEY) ?: return@setFragmentResultListener
            if(checkedField.ordinal != result.fieldIndex || checkedOrder != result.sort || tagId != result.tagId) {
                checkedField = FilesSorting.fromInteger(result.fieldIndex)
                checkedOrder = result.sort
                tagId = result.tagId

                viewModel.setTag(tagId)
                viewModel.setFilter("${checkedField.field},${checkedOrder.oder}")
                val folder: Folder = arguments?.getParcelable(FOLDER_KEY) ?: Folder.NULL_FOLDER
                viewModel.setFolderName(folder)
            }
        }
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
