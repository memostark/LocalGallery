package com.guillermonegrete.gallery

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FoldersViewModelTest {

    private lateinit var viewModel: FoldersViewModel

    private lateinit var settingsRepository: SettingsRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp(){
        settingsRepository = SettingsRepository()
        viewModel = FoldersViewModel(settingsRepository)
    }

    @Test
    fun show_message_when_no_url_set(){
        settingsRepository.serverUrl = ""

        viewModel.loadFolders()

        Assert.assertFalse(LiveDataTestUtil.getValue(viewModel.hasUrl))
    }
}