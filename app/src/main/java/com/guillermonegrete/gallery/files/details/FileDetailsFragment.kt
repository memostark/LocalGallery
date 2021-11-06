package com.guillermonegrete.gallery.files.details

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.guillermonegrete.gallery.MyApplication
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.databinding.FragmentFileDetailsBinding
import com.guillermonegrete.gallery.files.FilesViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class FileDetailsFragment : Fragment(R.layout.fragment_file_details) {

    private  var _binding: FragmentFileDetailsBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<FilesViewModel> { viewModelFactory }
    private var exoPlayer: SimpleExoPlayer? = null

    private var currentPlayerView: PlayerView? = null

    private val disposable = CompositeDisposable()

    private lateinit var adapter: FileDetailsAdapter

    override fun onAttach(context: Context) {
        (context.applicationContext as MyApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFileDetailsBinding.bind(view)

        adapter = FileDetailsAdapter()
        setUpViewModel()
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    private fun setUpViewModel() {
        binding.fileDetailsViewpager.adapter = adapter

        val index = arguments?.getInt(FILE_INDEX_KEY) ?: 0

        disposable.add(viewModel.cachedFileList
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    adapter.submitData(lifecycle, it)
                    binding.fileDetailsViewpager.setCurrentItem(index + 1, false)
                },
                { error -> Log.e(TAG, "Error loading files", error) }
            )
        )

        disposable.add(adapter.panelTouchSubject.distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({  panelTouched ->
                    // Prevents clunky sideways movement when dragging the bottom panel
                    binding.fileDetailsViewpager.isUserInputEnabled = !panelTouched
                },
                { error -> Log.e(TAG, "Error detecting panel touch", error) }
            )
        )
    }

    override fun onDestroyView() {
        binding.fileDetailsViewpager.adapter = null
        _binding = null
        disposable.clear()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        hideStatusBar()
    }

    override fun onStop() {
        super.onStop()
        showStatusBar()
        exoPlayer?.release()
        exoPlayer = null
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

        val viewPager: ViewPager2 = binding.fileDetailsViewpager
        setPagerListener(viewPager)
    }

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
        const val FILE_INDEX_KEY = "file_index"
        private val TAG = this::class.java.simpleName
    }
}
