package com.guillermonegrete.gallery.files.details

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.common.Order
import com.guillermonegrete.gallery.common.SortingDialog
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.databinding.FragmentFileDetailsBinding
import com.guillermonegrete.gallery.files.FilesViewModel
import com.guillermonegrete.gallery.files.SortField
import com.guillermonegrete.gallery.utils.hiltNavGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import kotlin.math.abs


@AndroidEntryPoint
class FileDetailsFragment : Fragment(R.layout.fragment_file_details) {

    private  var _binding: FragmentFileDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FilesViewModel by hiltNavGraphViewModels {
        if (isAllFiles) R.id.nav_graph else R.id.files_graph
    }
    private val args: FileDetailsFragmentArgs by navArgs()
    private var exoPlayer: ExoPlayer? = null

    private var currentPlayerView: PlayerView? = null

    private val disposable = CompositeDisposable()

    private lateinit var adapter: FileDetailsAdapter

    private var index = 0
    private var shouldSetIndex = false

    /**
     * The actual visibility of the system bars.
     */
    private var sysBarsVisible = true
        set(value) {
            adapter.showControls = value // Controls should be hidden when system bars are hidden
            field = value
        }

    /**
     *  The visibility the bars should have.
     *  It may differ from the actual visibility for older devices because if the bars are modified (e.g. the keyboard shows them) they don't return to their previous state.
     */
    private var showBars = true

    private var autoplayVideo = false

    private var bottomInset = 0

    private var checkedField = SortField.DEFAULT.field
    private var checkedOrder = Order.DEFAULT.oder
    private var tagId = SortingDialog.NO_TAG_ID

    private val isAllFiles: Boolean
        get() = args.folder == Folder.NULL_FOLDER

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.getFilter()?.let { filter ->
            checkedField = filter.sortType
            checkedOrder = filter.order
        }
        viewModel.getTag()?.let { tagId = it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(AddTagFragment.REQUEST_KEY) { _, result ->
            // Instead of using the snapshot list, the recommended approach to update an item is updating a cache source
            // and reloading from there (like a database) as explained here: https://stackoverflow.com/a/63139535/10244759
            // However this reload may force to reload the images which may be more wasteful
            val newTags = BundleCompat.getParcelableArrayList(result, AddTagFragment.TAGS_KEY, Tag::class.java) ?: return@setFragmentResultListener
            val pos = binding.fileDetailsViewpager.currentItem
            adapter.snapshot().items[pos].tags = newTags
            adapter.notifyItemChanged(pos)
        }
        index = savedInstanceState?.getInt(SAVED_INDEX_KEY) ?: args.fileIndex
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFileDetailsBinding.bind(view)

        shouldSetIndex = true

        adapter = FileDetailsAdapter()
        adapter.isAllFilesDest = findNavController().previousBackStackEntry?.destination?.id == R.id.all_files_dest

        autoplayVideo = viewModel.isAutoplayEnabled()

        val decorView = requireActivity().window.decorView
        ViewCompat.setOnApplyWindowInsetsListener(decorView) { v, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            bottomInset = systemInsets.bottom
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // The isVisible() method only works for API 30+, for older API use the deprecated visibility listener.
                val status = insets.isVisible(WindowInsetsCompat.Type.statusBars())
                val nav = insets.isVisible(WindowInsetsCompat.Type.navigationBars())
                sysBarsVisible = status || nav
                if (systemInsets.bottom != adapter.bottomInset) {
                    adapter.updateInsets(bottomInset)
                }
            } else {
                adapter.bottomInset = systemInsets.bottom
                // For older APIs, it's only necessary to listen once because the values never change (possibly a library bug)
                ViewCompat.setOnApplyWindowInsetsListener(decorView, null)
            }

            ViewCompat.onApplyWindowInsets(v, insets)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                val newVisibility = visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0
                if (sysBarsVisible != newVisibility) {
                    sysBarsVisible = newVisibility
                    val insets = if (newVisibility) bottomInset else 0
                    adapter.updateInsets(insets)
                }
            }
        }

        setUpViewModel()
    }

    @UnstableApi
    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVED_INDEX_KEY, index)
    }

    private fun setUpViewModel() {
        val viewpager = binding.fileDetailsViewpager
        viewpager.adapter = adapter

        viewModel.setFolderName(args.folder)
        disposable.addAll(
            viewModel.cachedFileList
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { adapter.submitData(lifecycle, it) },
                { error -> Timber.e(error, "Error loading files") }
            ),
            viewModel.detailsState.subscribe {
                adapter.setSheet(it.sheetVisible)
            },
            adapter.panelTouchSubject.distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ sheetState ->
                    val panelTouched = when(sheetState){
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            viewModel.setSheet(false)
                            false
                        }
                        BottomSheetBehavior.STATE_EXPANDED  -> {
                            viewModel.setSheet(true)
                            false
                        }
                        BottomSheetBehavior.STATE_DRAGGING -> true
                        else -> false
                    }

                    // Prevents clunky sideways movement when dragging the bottom panel
                    viewpager.isUserInputEnabled = !panelTouched
                },
                { error -> Timber.e(error, "Error detecting panel touch") }
            ),
            adapter.setCoverSubject.subscribe(
                { fileId -> updateFolderCover(fileId) },
                { error -> Timber.e(error, "On set cover click error") }
            ),
            adapter.onImageTapSubject.subscribe(
                { toggleBars() },
                { error -> Timber.e(error, "On image tap error") }
            ),
            adapter.onFolderIconTap.subscribe {
                index = viewpager.currentItem
                val action = FileDetailsFragmentDirections.fileDetailsToFilesFragment(it)
                findNavController().navigate(action)
            },
        )

        adapter.addLoadStateListener(loadListener)

        viewpager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                index = position
            }
        })
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
        adapter.removeLoadStateListener(loadListener)
        disposable.clear()
        binding.fileDetailsViewpager.adapter = null
        _binding = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
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
        val window = activity?.window ?: return
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        showBars = false
    }

    private fun showStatusBar(){
        val window = activity?.window ?: return
        WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
        showBars = true
    }

    @UnstableApi
    private fun initializePlayer(){
        if (exoPlayer == null) exoPlayer = ExoPlayer.Builder(requireContext()).build()

        val viewPager = binding.fileDetailsViewpager
        setPagerListener(viewPager)
    }

    // "Used for media3, APIs are safe we just use this to remove the warnings, see more: https://developer.android.com/guide/topics/media/media3/getting-started/migration-guide#unstableapi"
    @UnstableApi
    @SuppressLint("ClickableViewAccessibility")
    private fun setPagerListener(viewPager: ViewPager2){

        viewPager.setPageTransformer { page, position ->

            if (position == 0.0f){ // New page
                val pageIndex = viewPager.currentItem

                currentPlayerView?.setControllerVisibilityListener(null as PlayerView.ControllerVisibilityListener?)
                val player = exoPlayer ?: return@setPageTransformer
                player.pause()

                // If playerView exists it means is a video item, create Media Source and setup ExoPlayer
                val playerView: PlayerView = page.findViewById(R.id.exo_player_view) ?: return@setPageTransformer
                val file = adapter.snapshot()[pageIndex] ?: return@setPageTransformer

                val controllerListener = PlayerView.ControllerVisibilityListener {
                    val isVisible = it == View.VISIBLE
                    if (isVisible) showStatusBar() else hideStatusBar()
                    playerView.updatePadding(bottom = if (isVisible) bottomInset else 0)
                }
                val oldId = player.currentMediaItem?.mediaId
                // Detach player from previous view and update with current view
                if (currentPlayerView != playerView) currentPlayerView?.player = null
                playerView.player = player
                currentPlayerView = playerView

                if (oldId == file.name) {
                    if (autoplayVideo) player.play()
                    if(showBars) {
                        playerView.showController()
                    } else {
                        playerView.hideController()
                    }
                    playerView.setControllerVisibilityListener(controllerListener)

                    return@setPageTransformer
                }
                // Disable automatically changing the controls visibility.
                playerView.controllerAutoShow = sysBarsVisible
                playerView.controllerShowTimeoutMs = 0
                if(showBars) {
                    playerView.showController()
                } else {
                    playerView.hideController()
                }

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

                val detector = GestureDetector(requireContext(), MyGestureListener(playerView))
                playerView.setOnTouchListener { _, event -> return@setOnTouchListener detector.onTouchEvent(event) }
                playerView.setControllerVisibilityListener(controllerListener)

                val newItem = MediaItem.Builder().setMediaId(file.name).setUri(file.name).build()
                player.setMediaItem(newItem)
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
            val toHere = destination.id == R.id.file_details_dest
            if(toHere && (sysBarsVisible != showBars )) {
                if (!showBars) hideStatusBar()
            }
        }
    }

    private inner class MyGestureListener(val playerView: PlayerView) : GestureDetector.SimpleOnGestureListener() {

        val player = playerView.player

        val screenWidth = resources.displayMetrics.widthPixels
        val thirdWidth = screenWidth / 3

        @UnstableApi
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
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            e1 ?: return false
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x
            // Detects horizontal swipes in any direction
            if (abs(diffY) > abs(diffX)) {
                if (diffY < -SWIPE_THRESHOLD && velocityY < -SWIPE_VELOCITY_THRESHOLD) {
                    viewModel.setSheet(true)
                    return true
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }

    }

    private val loadListener  = { loadStates: CombinedLoadStates ->
        val state = loadStates.source
        if (shouldSetIndex &&
            state.refresh is LoadState.NotLoading &&
            state.append is LoadState.NotLoading) {
            // Try to set the item, if the index is bigger than the item count, the adapter will load more items and the listener will trigger again.
            binding.fileDetailsViewpager.setCurrentItem(index, false)
            // Once the count reaches the index, the item is set. Shouldn't set anymore.
            if (adapter.itemCount > index) shouldSetIndex = false
        }
    }

    companion object{

        const val FOLDER_UPDATE_KEY = "folder-updated"
        const val COVER_URL_KEY = "cover-url"
        const val SAVED_INDEX_KEY = "list_index"

        const val SWIPE_THRESHOLD = FileDetailsAdapter.SWIPE_THRESHOLD
        const val SWIPE_VELOCITY_THRESHOLD = FileDetailsAdapter.SWIPE_VELOCITY_THRESHOLD
    }
}
