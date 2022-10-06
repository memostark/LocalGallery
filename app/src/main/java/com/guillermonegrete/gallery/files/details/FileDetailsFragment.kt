package com.guillermonegrete.gallery.files.details

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.guillermonegrete.gallery.MyApplication
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.databinding.FragmentFileDetailsBinding
import com.guillermonegrete.gallery.files.FilesViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class FileDetailsFragment : Fragment(R.layout.fragment_file_details) {

    private  var _binding: FragmentFileDetailsBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by activityViewModels<FilesViewModel> { viewModelFactory }
    private var exoPlayer: ExoPlayer? = null

    private var currentPlayerView: StyledPlayerView? = null

    private val disposable = CompositeDisposable()

    private lateinit var adapter: FileDetailsAdapter

    private var index = 0

    override fun onAttach(context: Context) {
        (context.applicationContext as MyApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(AddTagFragment.REQUEST_KEY) { _, result ->
            // Instead of using the snapshot list, the recommended approach to update an item is updating a cache source
            // and reloading from there (like a database) as explained here: https://stackoverflow.com/a/63139535/10244759
            // However this reload may force to reload the images which may be more wasteful
            val newTags = result.getParcelableArrayList<Tag>(AddTagFragment.TAGS_KEY) ?: return@setFragmentResultListener
            val pos = binding.fileDetailsViewpager.currentItem
            adapter.snapshot().items[pos].tags = newTags
            adapter.notifyItemChanged(pos)
        }

        setUIChangesListeners()
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

    @SuppressLint("NotifyDataSetChanged")
    private fun setUpViewModel() {
        val viewpager = binding.fileDetailsViewpager
        viewpager.adapter = adapter

        index = arguments?.getInt(FILE_INDEX_KEY) ?: 0

        disposable.add(viewModel.cachedFileList
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    adapter.submitData(lifecycle, it)
                    viewpager.setCurrentItem(index, false)
                },
                { error -> Timber.e(error, "Error loading files") }
            )
        )

        disposable.add(adapter.panelTouchSubject.distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ panelTouched ->
                    // User finished touching, update adapter to update bottom sheet
                    if(!panelTouched) {
                        // Use post() to avoid: "IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling"
                        viewpager.post {
                            adapter.notifyDataSetChanged()
                        }
                    }

                    // Prevents clunky sideways movement when dragging the bottom panel
                    viewpager.isUserInputEnabled = !panelTouched
                },
                { error -> Timber.e(error, "Error detecting panel touch") }
            )
        )
    }

    override fun onDestroyView() {
        val pagerPos = binding.fileDetailsViewpager.currentItem
        viewModel.setNewPos(pagerPos)

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

    override fun onDestroy() {
        super.onDestroy()
        val decorView = activity?.window?.decorView ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            decorView.setOnApplyWindowInsetsListener(null)
        } else {
            @Suppress("DEPRECATION")
            decorView.setOnSystemUiVisibilityChangeListener(null)
        }
    }

    /**
     * Used to detect when the status bar becomes visible (for example when the keyboard shows up).
     * Hide again the bar if that's the case.
     */
    private fun setUIChangesListeners() {
        val decorView = activity?.window?.decorView ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            decorView.setOnApplyWindowInsetsListener { _, insets ->
                if (insets.isVisible(WindowInsetsCompat.Type.systemBars())) hideStatusBar()
                insets
            }
        } else {
            @Suppress("DEPRECATION")
            decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                // System bars are visible
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) hideStatusBar()
            }
        }
    }

    private fun hideStatusBar(){
        (activity as AppCompatActivity?)?.supportActionBar?.hide()

        val window = activity?.window ?: return
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showStatusBar(){
        (activity as AppCompatActivity?)?.supportActionBar?.show()

        val window = activity?.window ?: return
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
    }

    private fun initializePlayer(){
        if (exoPlayer == null) exoPlayer = ExoPlayer.Builder(requireContext()).build()

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
                val playerView: StyledPlayerView = page.findViewById(R.id.exo_player_view) ?: return@setPageTransformer

                // Detach player from previous view and update with current view
                currentPlayerView?.player = null
                currentPlayerView = playerView
                playerView.player = player

                val file = adapter.snapshot()[pageIndex] ?: return@setPageTransformer

                player.setMediaItem(MediaItem.fromUri(file.name))
                player.prepare()
                player.repeatMode = Player.REPEAT_MODE_ONE
                player.playWhenReady = false
            }
        }
    }

    companion object{
        const val FILE_INDEX_KEY = "file_index"
    }
}
