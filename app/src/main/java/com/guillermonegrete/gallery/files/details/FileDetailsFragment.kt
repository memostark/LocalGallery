package com.guillermonegrete.gallery.files.details

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.guillermonegrete.gallery.MyApplication
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.files.FilesViewModel
import javax.inject.Inject

class FileDetailsFragment : Fragment() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<FilesViewModel> { viewModelFactory }
    private var exoPlayer: SimpleExoPlayer? = null

    private var currentPlayerView: PlayerView? = null

    override fun onAttach(context: Context) {
        (context.applicationContext as MyApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_file_details, container, false)

        val index = arguments?.getInt(FILE_INDEX_KEY) ?: 0

        val viewPager: ViewPager2 = root.findViewById(R.id.file_details_viewpager)

        val fileList = viewModel.cachedFileList

        exoPlayer = SimpleExoPlayer.Builder(requireContext()).build()

        viewPager.adapter = FileDetailsAdapter(fileList)
        viewPager.currentItem = index

        setPagerListener(viewPager)

        return root
    }

    override fun onResume() {
        super.onResume()
        hideStatusBar()
    }

    override fun onStop() {
        super.onStop()
        showStatusBar()
        exoPlayer?.release()
    }

    private fun hideStatusBar(){
        (activity as AppCompatActivity).supportActionBar?.hide()

        val window = activity?.window
        if (Build.VERSION.SDK_INT < 16) {
            window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }else{
            window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }

    }

    private fun showStatusBar(){
        (activity as AppCompatActivity).supportActionBar?.show()

        val window = activity?.window
        if (Build.VERSION.SDK_INT < 16) {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }else{
            window?.decorView?.systemUiVisibility = 0
        }
    }

    private fun setPagerListener(viewPager: ViewPager2){

        viewPager.setPageTransformer { page, position ->

            if (position == 0.0f){ // New page
                val playerView: PlayerView? = page.findViewById(R.id.exo_player_view)
                val pageIndex = viewPager.currentItem

                // If playerView exists it means is a video item, create Media Source and setup ExoPlayer
                playerView?.let {
                    val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(requireContext(), "player"))
                    val file = viewModel.cachedFileList[pageIndex]

                    val extractorMediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(file.name))

                    // Detach player from previous view and update with current view
                    currentPlayerView?.player = null
                    currentPlayerView = it
                    it.player = exoPlayer

                    exoPlayer?.prepare(extractorMediaSource)
                    exoPlayer?.playWhenReady = false
                }
            }
        }
    }

    companion object{
        const val FILE_INDEX_KEY = "file_index"
    }
}
