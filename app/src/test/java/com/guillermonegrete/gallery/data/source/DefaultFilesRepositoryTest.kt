package com.guillermonegrete.gallery.data.source

import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.FileResponse
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
        listOf(FileResponse("file1.jpg", 0, 0), FileResponse("file2.jpg", 0, 0)),
        listOf(FileResponse("video.mp4", 0, 0), FileResponse("file5.jpg", 0, 0)),
        listOf(FileResponse("file7.png", 0, 0), FileResponse("file5.jpg", 0, 0))
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

        val expected = listOf(File("file1.jpg", "jpg"), File("file2.jpg", "jpg"))
        files.test()
            .assertValue(expected)
    }
}