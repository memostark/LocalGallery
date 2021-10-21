package com.guillermonegrete.gallery.files

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.flatMap
import androidx.paging.map
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.guillermonegrete.gallery.MyApplication
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.databinding.FragmentFilesListBinding
import com.guillermonegrete.gallery.files.AspectRatioComputer.getAspectRatio
import com.guillermonegrete.gallery.files.AspectRatioComputer.normalizeHeights
import com.guillermonegrete.gallery.files.AspectRatioComputer.updateSizes
import com.guillermonegrete.gallery.files.details.FileDetailsAdapter
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
    private val detailsDisposable = CompositeDisposable()

    private lateinit var adapter: FilesAdapter

    private var exoPlayer: SimpleExoPlayer? = null
    private var currentPlayerView: PlayerView? = null

    private var inDetailsView = false

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

        // Handles the back button, used for restoring the list layout when navigating back from details view pager
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if(inDetailsView) {
                showListView()
            } else {
                if (isEnabled) {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }
    }

    private fun showListView() {
        with(binding){
            fileDetailsViewpager.isVisible = false
            filesList.isVisible = true
            showStatusBar()
            detailsDisposable.clear()
        }

        showStatusBar()
        exoPlayer?.release()
        exoPlayer = null
        inDetailsView = false
    }

    override fun onStart() {
        super.onStart()
        setFileClickEvent()
        if(inDetailsView) initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if(inDetailsView) hideStatusBar()
    }

    override fun onStop() {
        super.onStop()
        if(inDetailsView) {
            showStatusBar()
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    override fun onDestroyView() {
        binding.filesList.adapter = null
        _binding = null
        disposable.clear()
        adapter.removeLoadStateListener(loadListener)
        super.onDestroyView()
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

        val screenWidth = getScreenWidth()

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
                            normalizeHeights(tempSizes, width / arSum, screenWidth)
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
                            normalizeHeights(tempSizes, width / arSum, screenWidth)
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
                            normalizeHeights(tempSizes, width / arSum, screenWidth)
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
            .subscribe(::openFileDetails)
        )
    }

    private fun openFileDetails(index: Int){
        inDetailsView = true
        val folder = arguments?.getString(FOLDER_KEY) ?: ""
        hideStatusBar()
        initializePlayer()

        val pagerAdapter = FileDetailsAdapter()
        with(binding){
            fileDetailsViewpager.isVisible = true
            fileDetailsViewpager.adapter = pagerAdapter

            filesList.isVisible = false
            detailsDisposable.add(viewModel.loadPagedFiles(folder)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        pagerAdapter.submitData(lifecycle, it)
                        binding.fileDetailsViewpager.setCurrentItem(index + 1, false)
                    },
                    { error -> println("Error loading files: ${error.message}") }
                )
            )
        }
    }

    private val loadListener  = { loadStates: CombinedLoadStates ->
        val state = loadStates.refresh
        binding.loadingIcon.isVisible = state is LoadState.Loading
        binding.filesMessageContainer.isVisible = state is LoadState.Error
        if(state is LoadState.Error) Log.e("FilesFileFragment", "Error when loading", state.error)
    }

    data class Size(var width: Int, var height: Int)

    private fun Fragment.getScreenWidth(): Int{
        val dm = DisplayMetrics()
        this.requireActivity().windowManager.defaultDisplay.getMetrics(dm)
        return dm.widthPixels
    }

    private fun hideStatusBar(){
        (activity as AppCompatActivity).supportActionBar?.hide()

        val window = activity?.window ?: return
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

    }

    private fun showStatusBar(){
        (activity as AppCompatActivity).supportActionBar?.show()

        val window = activity?.window ?: return
        window.decorView.systemUiVisibility = 0
    }

    private fun initializePlayer(){
        if (exoPlayer == null) exoPlayer = SimpleExoPlayer.Builder(requireContext()).build()

        setPagerListener(binding.fileDetailsViewpager)
    }

    /**
     * This pager listener is used when swiping to a page video to configure the video source and player
     */
    private fun setPagerListener(viewPager: ViewPager2){

        viewPager.setPageTransformer { page, position ->

            if (position == 0.0f){ // New page
                val pageIndex = viewPager.currentItem

                val player = exoPlayer ?: return@setPageTransformer
                player.stop()
                player.seekTo(0)

                // If playerView exists it means is a video item, create Media Source and setup ExoPlayer
                val playerView: PlayerView = page.findViewById(R.id.exo_player_view) ?: return@setPageTransformer
                val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(requireContext(), "player"))

                // Detach player from previous view and update with current view
                currentPlayerView?.player = null
                currentPlayerView = playerView
                playerView.player = player

                val file = adapter.snapshot()[pageIndex] ?: return@setPageTransformer

                val extractorMediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(file.name))

                player.prepare(extractorMediaSource)
                player.repeatMode = Player.REPEAT_MODE_ONE
                player.playWhenReady = false
            }
        }
    }

    companion object{
        const val FOLDER_KEY = "folder"
    }
}