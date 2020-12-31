package com.guillermonegrete.gallery.files

import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.source.FakeFilesRepository
import com.guillermonegrete.gallery.data.source.FakeSettingsRepository
import org.junit.Before
import org.junit.Test

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
    fun `Load paged files`(){
        // TODO find out how to test paging 3 library
    }
}