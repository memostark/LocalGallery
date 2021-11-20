package com.guillermonegrete.gallery.folders

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.source.FakeFilesRepository
import com.guillermonegrete.gallery.data.source.FakeSettingsRepository
import com.guillermonegrete.gallery.folders.models.FolderUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FoldersViewModelTest {

    // Necessary when using paging "cachedIn" in the view model.
    private val dispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(dispatcher)


    private lateinit var viewModel: FoldersViewModel

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var filesRepository: FakeFilesRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val defaultFolders = listOf(
        Folder("first", "", 0),
        Folder("second", "", 0)
    )
    private val defaultUIFolders = listOf(
        FolderUI.HeaderModel(""),
        FolderUI.Model("first", "", 0),
        FolderUI.Model("second", "", 0)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Before
    fun setUp(){
        settingsRepository = FakeSettingsRepository()
        filesRepository = FakeFilesRepository()
        viewModel = FoldersViewModel(
            settingsRepository,
            filesRepository
        )

        filesRepository.addFolders(*defaultFolders.toTypedArray())
    }

    @Test
    fun `Given empty url, when loading folders, emit url available false`(){
        settingsRepository.serverUrl = ""

        // When url is not set. flow shouldn't be emitting
        viewModel.pagedFolders.test()
            .assertEmpty()

        // Url not set
        viewModel.urlAvailable.test()
            .assertValues(false)
    }

    @Test
    fun `Given url set, when loading folders, emit url available and folders`() {
        // Has url set
        settingsRepository.serverUrl = "url"

        // Sets observer, otherwise flow won't emit
        viewModel.pagedFolders.test()

        viewModel.getFolders()

        // Url is set
        viewModel.urlAvailable.test()
            .assertValues(true)

        // Assert default items emitted
        val resultPaging = viewModel.pagedFolders.blockingFirst()
        val resultItems = getItems(resultPaging)
        assertEquals(defaultUIFolders, resultItems)
    }

    @Test
    fun load_preset_address_dialog_data(){
        val savedURL = "preset-url"
        settingsRepository.serverUrl = savedURL

        viewModel.getDialogData()
            .test()
            .assertValues(savedURL)
    }

    @Test
    fun `Given empty url, when url changed, then folders reload`(){
        // save new server address
        val newURL = "new-url"
        viewModel.updateServerUrl(newURL)
        viewModel.pagedFolders.test()
        viewModel.getFolders()

        // Assert new url set
        assertEquals(settingsRepository.serverUrl, newURL)
        assertEquals(filesRepository.repoUrl, newURL)

        // Assert default items emitted
        val items = getItems(viewModel.pagedFolders.blockingFirst())
        assertEquals(defaultUIFolders, items)
    }

    @Test
    fun `Given no folders in root, when load, no folders layout shown`(){
        viewModel.pagedFolders.test()

        // Set folders list as empty
        filesRepository.foldersServiceData = arrayListOf()

        // Set valid URL
        val savedURL = "preset-url"
        settingsRepository.serverUrl = savedURL

        // When
        viewModel.getFolders()

        val items = getItems(viewModel.pagedFolders.blockingFirst())
        assertEquals(emptyList<FolderUI>(), items)
    }

    /**
     * This is the only way to extract items from PagingData as explained here:
     * https://developer.android.com/topic/libraries/architecture/paging/test#transformation-tests
     */
    private fun getItems(folders: PagingData<FolderUI>): List<FolderUI> {
        val differ = AsyncPagingDataDiffer(
            diffCallback = MyDiffCallback(),
            updateCallback = NoopListCallback(),
            workerDispatcher = Dispatchers.Main
        )
        testScope.runBlockingTest {
            differ.submitData(folders)
            advanceUntilIdle()
        }

        return differ.snapshot().items
    }

    class NoopListCallback : ListUpdateCallback {
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
    }

    class MyDiffCallback : DiffUtil.ItemCallback<FolderUI>() {
        override fun areItemsTheSame(oldItem: FolderUI, newItem: FolderUI) = oldItem == newItem

        override fun areContentsTheSame(oldItem: FolderUI, newItem: FolderUI) = oldItem == newItem
    }

}
