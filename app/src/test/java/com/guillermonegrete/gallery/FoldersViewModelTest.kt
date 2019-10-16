package com.guillermonegrete.gallery

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.guillermonegrete.gallery.data.source.FakeFilesRepository
import com.guillermonegrete.gallery.data.source.FakeSettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FoldersViewModelTest {

    private lateinit var viewModel: FoldersViewModel

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var filesRepository: FakeFilesRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val defaultFolders = listOf("first", "second")

    @Before
    fun setUp(){
        settingsRepository = FakeSettingsRepository()
        filesRepository = FakeFilesRepository()
        viewModel = FoldersViewModel(settingsRepository, filesRepository)

        filesRepository.addFolders(*defaultFolders.toTypedArray())
    }

    @Test
    fun show_message_when_no_url_set(){
        settingsRepository.serverUrl = ""

        viewModel.loadFolders()

        assertFalse(LiveDataTestUtil.getValue(viewModel.hasUrl))
    }

    @Test
    fun loads_folders(){
        settingsRepository.serverUrl = "url"

        viewModel.loadFolders()

        val folders = LiveDataTestUtil.getValue(viewModel.folders)
        assertEquals(folders.size, 2)
    }

    @Test
    fun load_set_address_dialog_data(){
        val savedURL = "preset-url"
        settingsRepository.serverUrl = savedURL

        viewModel.loadDialogData()

        val data = LiveDataTestUtil.getValue(viewModel.openDialog)
        assertEquals(savedURL, data)
    }

    @Test
    fun when_server_changed_reload_folders(){
        // save new server address
        val newURL = "new-url"
        viewModel.updateUrl(newURL)

        // Assert new url set
        assertEquals(settingsRepository.serverUrl, newURL)
        assertEquals(filesRepository.repoUrl, newURL)

        // load folders with new address
        val folders = LiveDataTestUtil.getValue(viewModel.folders)
        assertEquals(folders.size, 2)
    }

    @Test
    fun show_message_observable_when_no_url_set(){
        settingsRepository.serverUrl = ""

        viewModel.getFolders().test()
            .assertComplete()
            .assertValue(emptyList())

        // Url not set
        viewModel.urlAvailable.test()
            .assertValues(false)
    }

    @Test
    fun load_folders_with_observables(){
        // Has url set
        settingsRepository.serverUrl = "url"

        // Return list correctly
        viewModel.getFolders().test()
            .assertComplete()
            .assertValue(defaultFolders)

        // Url is set
        viewModel.urlAvailable.test()
            .assertValues(true)
    }
}