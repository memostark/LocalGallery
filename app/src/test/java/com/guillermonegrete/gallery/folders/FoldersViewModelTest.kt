package com.guillermonegrete.gallery.folders

import android.util.Log
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.testing.asSnapshot
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.source.FakeFilesRepository
import com.guillermonegrete.gallery.data.source.FakeSettingsRepository
import com.guillermonegrete.gallery.folders.models.FolderUI
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FoldersViewModelTest {

    // Necessary when using paging "cachedIn" in the view model.
    private val testScope = TestScope()
    private val testDispatcher = UnconfinedTestDispatcher(testScope.testScheduler)


    private lateinit var viewModel: FoldersViewModel

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var filesRepository: FakeFilesRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val defaultFolders = listOf(
        Folder("first", "", 0, 1),
        Folder("second", "", 0, 2)
    )
    private val defaultUIFolders = listOf(
        FolderUI.HeaderModel(""),
        FolderUI.Model("first", "", 0, 1),
        FolderUI.Model("second", "", 0, 2)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Paging data now uses this class which depends on the Android framework
        mockkStatic(Log::class)
        every { Log.isLoggable(any(), any()) } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    @Before
    fun setUp(){
        mockkStatic(View::class)
        every { View.generateViewId() } returns 10
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

        val urlObserver = viewModel.urlAvailable.test()
        val foldersObserver = viewModel.pagedFolders.test()

        viewModel.getFolders()
        // When url is not set. flow shouldn't be emitting
        foldersObserver.assertEmpty()

        // Url not set
        urlObserver.assertValues(false)
    }

    @Test
    fun `Given url set, when loading folders, emit url available and folders`() = runTest {
        // Has url set
        settingsRepository.serverUrl = "url"

        // Sets observer, otherwise flow won't emit
        val urlObserver = viewModel.urlAvailable.test()
        val folderObserver = viewModel.pagedFolders.take(1)

        viewModel.getFolders()

        // Url is set
        urlObserver.assertValues(true)

        // Assert default items emitted
        assertEquals(defaultUIFolders, folderObserver.asFlow().asSnapshot())
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
    fun `Given empty url, when url changed, then folders reload`() = runTest {
        // save new server address
        val newURL = "new-url"
        viewModel.updateServerUrl(newURL)
        val folderObserver = viewModel.pagedFolders.take(1)
        viewModel.getFolders()

        // Assert new url set
        assertEquals(settingsRepository.serverUrl, newURL)

        // Assert default items emitted
        assertEquals(defaultUIFolders, folderObserver.asFlow().asSnapshot())
    }

    @Test
    fun `Given no folders in root, when load, no folders layout shown`() = runTest {
        val folderObserver = viewModel.pagedFolders.take(1)

        // Set folders list as empty
        filesRepository.foldersServiceData = arrayListOf()

        // Set valid URL
        val savedURL = "preset-url"
        settingsRepository.serverUrl = savedURL

        // When
        viewModel.getFolders()

        folderObserver.asFlow().asSnapshot().isEmpty()
    }

}
