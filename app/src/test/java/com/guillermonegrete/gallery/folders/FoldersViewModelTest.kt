package com.guillermonegrete.gallery.folders

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.source.FakeFilesRepository
import com.guillermonegrete.gallery.data.source.FakeSettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.RuntimeException

class FoldersViewModelTest {

    private lateinit var viewModel: FoldersViewModel

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var filesRepository: FakeFilesRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val defaultFolders = listOf(
        Folder("first", "", 0),
        Folder("second", "", 0)
    )

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

    @Test
    fun load_preset_address_dialog_data(){
        val savedURL = "preset-url"
        settingsRepository.serverUrl = savedURL

        viewModel.getDialogData()
            .test()
            .assertValues(savedURL)
    }

    @Test
    fun when_server_address_changed_reload_folders(){
        // save new server address
        val newURL = "new-url"
        viewModel.updateServerUrl(newURL)

        // Assert new url set
        assertEquals(settingsRepository.serverUrl, newURL)
        assertEquals(filesRepository.repoUrl, newURL)

        // load folders with new address
        viewModel.getFolders().test()
            .assertComplete()
            .assertValue(defaultFolders)
    }

    @Test
    fun show_error_layout_when_loading(){
        val networkTest = viewModel.networkError.test()

        filesRepository.setReturnError(true)

        // Set valid URL
        val savedURL = "preset-url"
        settingsRepository.serverUrl = savedURL

        viewModel.getFolders().test()
            .assertError(RuntimeException::class.java)

        networkTest.assertValue(true)
    }

    @Test
    fun show_no_folders_in_root_folder_layout(){
        val rootFolderEmptyTest = viewModel.rootFolderEmpty.test()

        // Set folders list as empty
        filesRepository.foldersServiceData = arrayListOf()

        // Set valid URL
        val savedURL = "preset-url"
        settingsRepository.serverUrl = savedURL

        viewModel.getFolders().test()
            .assertValue(arrayListOf())

        rootFolderEmptyTest.assertValue(true)
    }
}