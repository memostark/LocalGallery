package com.guillermonegrete.gallery.data.source

import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.GetFolderResponse
import com.guillermonegrete.gallery.data.source.remote.FakeFileServerAPI
import org.junit.Before
import org.junit.Test

class DefaultFilesRepositoryTest {

    private val defaultFolders = listOf(
        Folder("first", "", 5),
        Folder("second", "", 3),
        Folder("third", "", 11)
    )
    private val defaultFiles = listOf(
        listOf("file1.jpg", "file2.jpg"),
        listOf("video.mp4", "file5.jpg"),
        listOf("file7.png", "file8.jpg")
    )
    private val defaultGetFolderResponse = GetFolderResponse("Name", defaultFolders)
    private lateinit var repository: DefaultFilesRepository

    @Before
    fun createRepository(){
        val filesMap = linkedMapOf(
            defaultFolders[0] to defaultFiles[0],
            defaultFolders[1] to defaultFiles[1],
            defaultFolders[2] to defaultFiles[2]
        )

        repository = DefaultFilesRepository(FakeFileServerAPI(filesMap))
    }

    @Test
    fun getFolders_first_time(){
        val folders = repository.getFolders()
        folders.test()
            .assertValue(defaultGetFolderResponse)
    }

    @Test
    fun getFiles_first_time(){
        val folderName = defaultFolders.first().name
        val files = repository.getFiles(folderName)
        files.test()
            .assertValue(defaultFiles.first())
    }
}