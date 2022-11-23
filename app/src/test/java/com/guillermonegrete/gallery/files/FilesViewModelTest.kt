package com.guillermonegrete.gallery.files

import com.guillermonegrete.gallery.data.ImageFile
import com.guillermonegrete.gallery.data.source.FakeFilesRepository
import com.guillermonegrete.gallery.data.source.FakeSettingsRepository
import org.junit.Before
import org.junit.Test

class FilesViewModelTest {

    private lateinit var viewModel: FilesViewModel

    private lateinit var filesRepository: FakeFilesRepository

    private val defaultFolder = "folder-name"
    private val defaultFiles = listOf(
        ImageFile("http://file1.jpg", id = 1),
        ImageFile("http://file02.jpg", id = 2),
        ImageFile("http://file_3.jpg", id = 4)
    )

    @Before
    fun setUp(){
        filesRepository = FakeFilesRepository()
        viewModel = FilesViewModel(filesRepository)

        filesRepository.addFiles(defaultFolder, *defaultFiles.toTypedArray())
    }

    @Test
    fun `Load paged files`(){
        // TODO find out how to test paging 3 library
    }
}