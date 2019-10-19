package com.guillermonegrete.gallery.files

import com.guillermonegrete.gallery.data.source.FakeFilesRepository
import com.guillermonegrete.gallery.data.source.FakeSettingsRepository
import org.junit.Before
import org.junit.Test

class FilesViewModelTest {

    private lateinit var viewModel: FilesViewModel

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var filesRepository: FakeFilesRepository

    private val defaultFolder = "folder-name"
    private val defaultFiles = listOf("http://file1.jpg", "http://file02.jpg", "http://file_3.jpg")

    @Before
    fun setUp(){
        settingsRepository = FakeSettingsRepository()
        filesRepository = FakeFilesRepository()
        viewModel = FilesViewModel(settingsRepository, filesRepository)

        filesRepository.addFiles(defaultFolder, *defaultFiles.toTypedArray())
    }

    @Test
    fun load_files(){
        // Has url set
        settingsRepository.serverUrl = "url"

        viewModel.loadFiles(defaultFolder).test()
            .assertComplete()
            .assertValue(defaultFiles)
    }
}