package com.guillermonegrete.gallery.data.source

import com.guillermonegrete.gallery.data.*
import com.guillermonegrete.gallery.data.source.remote.FakeFileServerAPI
import com.guillermonegrete.gallery.folders.source.FakeFoldersApi
import org.junit.Before
import org.junit.Test
import java.util.*

class DefaultFilesRepositoryTest {

    private val defaultFolders = listOf(
        Folder("first", "", 5),
        Folder("second", "", 3),
        Folder("third", "", 11)
    )
    private val defaultFiles = listOf(
        listOf(ImageFileResponse("file1.jpg", 0, 0), ImageFileResponse("file2.jpg", 0, 0)),
        listOf(VideoFileResponse("video.mp4", 0, 0, 3), ImageFileResponse("file5.jpg", 0, 0)),
        listOf(ImageFileResponse("file7.png", 0, 0), ImageFileResponse("file5.jpg", 0, 0))
    )
    private val defaultGetFolderResponse = GetFolderResponse("Name", defaultFolders)
    private lateinit var repository: DefaultFilesRepository

    @Before
    fun createRepository(){
        val filesMap = linkedMapOf(
            defaultFolders[0] to defaultFiles[0],
            defaultFolders[1] to defaultFiles[1],
        )

        val fileApi = FakeFileServerAPI(filesMap)
        repository = DefaultFilesRepository(fileApi, FakeFoldersApi())
        fileApi.addFolder(defaultFolders[2], defaultFiles[2])
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

        val date = Date()
        val expected = listOf(
            ImageFile("file1.jpg", creationDate = date, lastModified = date, id = 1),
            ImageFile("file2.jpg", creationDate = date, lastModified = date, id = 2)
        )
        files.test()
            .assertValue(expected)
    }
}