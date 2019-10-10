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

    @Before
    fun setUp(){
        settingsRepository = FakeSettingsRepository()
        filesRepository = FakeFilesRepository()
        viewModel = FoldersViewModel(settingsRepository, filesRepository)

        filesRepository.addFolders("first", "second")
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

        assertEquals(settingsRepository.serverUrl, newURL)

        // load folders with new address
        val folders = LiveDataTestUtil.getValue(viewModel.folders)
        assertEquals(folders.size, 2)
    }
}