package com.guillermonegrete.gallery

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.guillermonegrete.gallery.data.source.FakeFilesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FoldersViewModelTest {

    private lateinit var viewModel: FoldersViewModel

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var filesRepository: FakeFilesRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp(){
        settingsRepository = SettingsRepository()
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
}