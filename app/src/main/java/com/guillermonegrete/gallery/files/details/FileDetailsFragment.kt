package com.guillermonegrete.gallery.files.details

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.databinding.FragmentFileDetailsBinding
import com.guillermonegrete.gallery.files.FilesViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import kotlin.math.abs

// Used for media3, APIs are safe we just use this to remove the warnings, see more: https://developer.android.com/guide/topics/media/media3/getting-started/migration-guide#unstableapi
@UnstableApi
@AndroidEntryPoint
class FileDetailsFragment : Fragment(R.layout.fragment_file_details) {

    private  var _binding: FragmentFileDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FilesViewModel by activityViewModels()
    private var exoPlayer: ExoPlayer? = null

    private var currentPlayerView: PlayerView? = null

    private val disposable = CompositeDisposable()

    private lateinit var adapter: FileDetailsAdapter

    private var index = 0

    /**
     * The actual visibility of the system bars.
     */
    private var sysBarsVisible = false

    /**
     *  The visibility the bars should have.
     *  It may differ from the actual visibility for older devices because if the bars are modified (e.g. the keyboard shows them) they don't return to their previous state.
     */
    private var showBars = false

    private var autoplayVideo = false

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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFileDetailsBinding.bind(view)

        adapter = FileDetailsAdapter()
        adapter.isAllFilesDest = findNavController().previousBackStackEntry?.destination?.id == R.id.all_files_dest

        autoplayVideo = viewModel.isAutoplayEnabled()

        val decorView = requireActivity().window.decorView
        ViewCompat.setOnApplyWindowInsetsListener(decorView) { v, insets ->
            val status = insets.isVisible(WindowInsetsCompat.Type.statusBars())
            val nav = insets.isVisible(WindowInsetsCompat.Type.navigationBars())
            sysBarsVisible = status || nav
            // Return like this otherwise the insets show when they shouldn't
            ViewCompat.onApplyWindowInsets(v, insets)
        }

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

        disposable.addAll(
            viewModel.cachedFileList
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    adapter.submitData(lifecycle, it)
                    viewpager.setCurrentItem(index, false)
                },
                { error -> Timber.e(error, "Error loading files") }
            ),
            adapter.panelTouchSubject.distinctUntilChanged()
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
                    }, { error -> Timber.e(error, "Error detecting panel touch") })
            ,
            adapter.setCoverSubject.subscribe(
                { fileId -> updateFolderCover(fileId) },
                { error -> Timber.e(error, "On set cover click error") }
            ),
            adapter.onImageTapSubject.subscribe(
                { toggleBars() },
                { error -> Timber.e(error, "On image tap error") }
            )
        )
    }

    private fun toggleBars() {
        if (sysBarsVisible) {
            hideStatusBar()
        } else {
            showStatusBar()
        }
    }

    private fun updateFolderCover(fileId: Long) {
        disposable.add(
            viewModel.setCoverFile(fileId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        Toast.makeText(context, "Successfully updated folder cover", Toast.LENGTH_SHORT).show()
                        val newUrl = it.coverUrl
                        // notify the folder fragment that the cover was changed
                        setFragmentResult(FOLDER_UPDATE_KEY, bundleOf(COVER_URL_KEY to newUrl))
                    },
                    { error ->
                        Toast.makeText(context, "Couldn't update folder cover", Toast.LENGTH_SHORT).show()
                        Timber.e(error, "On set cover click error")
                    }
                )
        )
    }

    override fun onDestroyView() {
        val pagerPos = binding.fileDetailsViewpager.currentItem
        viewModel.setNewPos(pagerPos)

        val decorView = requireActivity().window.decorView
        ViewCompat.setOnApplyWindowInsetsListener(decorView, null)
        binding.fileDetailsViewpager.adapter = null
        _binding = null
        disposable.clear()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        hideStatusBar()
        findNavController().addOnDestinationChangedListener(listener)
    }

    override fun onPause() {
        super.onPause()
        findNavController().removeOnDestinationChangedListener(listener)
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

    private fun hideStatusBar(){
        (activity as AppCompatActivity?)?.supportActionBar?.hide()

        val window = activity?.window ?: return
        // Must be true, otherwise empty space will be left where the bottom navigation bar was located.
        // This affected mainly phones without physical navigation bar buttons
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        showBars = false
    }

    private fun showStatusBar(){
        (activity as AppCompatActivity?)?.supportActionBar?.show()

        val window = activity?.window ?: return
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())

        showBars = true
    }

    private fun initializePlayer(){
        if (exoPlayer == null) exoPlayer = ExoPlayer.Builder(requireContext()).build()

        val viewPager = binding.fileDetailsViewpager
        setPagerListener(viewPager)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setPagerListener(viewPager: ViewPager2){

        viewPager.setPageTransformer { page, position ->

            if (position == 0.0f){ // New page
                val pageIndex = viewPager.currentItem

                val player = exoPlayer ?: return@setPageTransformer
                player.stop()
                player.seekTo(0)

                // If playerView exists it means is a video item, create Media Source and setup ExoPlayer
                val playerView: PlayerView = page.findViewById(R.id.exo_player_view) ?: return@setPageTransformer

                // Detach player from previous view and update with current view
                currentPlayerView?.player = null
                currentPlayerView = playerView
                playerView.player = player
                // Controls hidden by default
                playerView.controllerAutoShow = false
                playerView.hideController()

                playerView.setShowRewindButton(false)
                playerView.setShowFastForwardButton(false)
                playerView.setShowPreviousButton(false)
                playerView.setShowNextButton(false)
                val toggleBtn: ImageButton = playerView.findViewById(R.id.toggle_audio)
                setAudio(player, toggleBtn)
                toggleBtn.setOnClickListener {
                    viewModel.audioOn = player.volume < 0.5f
                    setAudio(player, toggleBtn)
                }

                val detector = GestureDetectorCompat(requireContext(), MyGestureListener(playerView))
                playerView.setOnTouchListener { _, event -> return@setOnTouchListener detector.onTouchEvent(event) }

                val file = adapter.snapshot()[pageIndex] ?: return@setPageTransformer

                player.setMediaItem(MediaItem.fromUri(file.name))
                player.prepare()
                player.repeatMode = Player.REPEAT_MODE_ONE
                player.playWhenReady = autoplayVideo
            }
        }
    }

    private fun setAudio(player: Player, toggleBtn: ImageButton) {
        if(viewModel.audioOn) {
            player.volume = 1f
            toggleBtn.setImageResource(R.drawable.baseline_volume_up_24)
        } else {
            player.volume = 0f
            toggleBtn.setImageResource(R.drawable.baseline_volume_off_24)
        }
    }

    /**
     * Hide the bar if it's visible when navigating back here (usually from a dialog that shows the keyboard)
     */
    private val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
        // SDKs 30+ automatically hide the bar
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val toHere = destination.id == R.id.fileDetailsFragment
            if(toHere && (sysBarsVisible != showBars )) {
                if (!showBars) hideStatusBar()
            }
        }
    }

    private inner class MyGestureListener(val playerView: PlayerView) : GestureDetector.SimpleOnGestureListener() {

        val player = playerView.player

        val screenWidth = resources.displayMetrics.widthPixels
        val thirdWidth = screenWidth / 3

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (e.x < thirdWidth) {
                player?.seekBack()
                playerView.showController()
            } else if(e.x > (screenWidth - thirdWidth)) {
                player?.seekForward()
                playerView.showController()
            } else {
                return false
            }
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x
            // Detects horizontal swipes in any direction
            if (abs(diffY) > abs(diffX)) {
                if (diffY < -SWIPE_THRESHOLD && velocityY < -SWIPE_VELOCITY_THRESHOLD) {
                    adapter.showSheet(binding.fileDetailsViewpager.currentItem)
                    return true
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }

    }

    companion object{
        const val FILE_INDEX_KEY = "file_index"

        const val FOLDER_UPDATE_KEY = "folder-updated"
        const val COVER_URL_KEY = "cover-url"

        const val SWIPE_THRESHOLD = FileDetailsAdapter.SWIPE_THRESHOLD
        const val SWIPE_VELOCITY_THRESHOLD = FileDetailsAdapter.SWIPE_VELOCITY_THRESHOLD
    }
}
