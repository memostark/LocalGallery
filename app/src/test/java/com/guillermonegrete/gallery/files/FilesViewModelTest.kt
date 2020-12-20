package com.guillermonegrete.gallery.files

import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.source.FakeFilesRepository
import com.guillermonegrete.gallery.data.source.FakeSettingsRepository
import org.junit.Before
import org.junit.Test
import java.lang.RuntimeException

class FilesViewModelTest {

    private lateinit var viewModel: FilesViewModel

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var filesRepository: FakeFilesRepository

    private val defaultFolder = "folder-name"
    private val defaultFiles = listOf(
        File("http://file1.jpg", "jpg"),
        File("http://file02.jpg", "jpg"),
        File("http://file_3.jpg", "jpg")
    )

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

    @Test
    fun show_error_layout_when_exception_loading(){
        filesRepository.setReturnError(true)

        val savedURL = "preset-url"
        settingsRepository.serverUrl = savedURL

        viewModel.loadFiles(defaultFolder).test()
            .assertError(RuntimeException::class.java)

        viewModel.networkError.test()
            .assertValues(true)
    }
}