package com.guillermonegrete.gallery.data.source

import androidx.annotation.VisibleForTesting
import androidx.paging.PagingData
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.GetFolderResponse
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import java.lang.RuntimeException

class FakeFilesRepository: FilesRepository {

    var foldersServiceData = arrayListOf<Folder>()

    var filesServiceData: LinkedHashMap<String, MutableList<File>> = LinkedHashMap()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override fun getFolders(): Single<GetFolderResponse> {
        if(shouldReturnError) return Single.error(RuntimeException())
        return Single.just(GetFolderResponse("Name", foldersServiceData))
    }

    override fun getPagedFolders(tagIds: List<Long>, query: String?, sort: String?): Flowable<PagingData<Folder>> {
        return Flowable.just(PagingData.from(foldersServiceData))
    }

    override fun getFiles(folder: String): Single<List<File>> {
        if(shouldReturnError) return Single.error(RuntimeException())
        val files = filesServiceData[folder]
        return if(files == null) Single.error(RuntimeException("Couldn't find entry for folder: $folder")) else Single.just(files)
    }

    override fun getPagedFiles(
        folder: Folder,
        tagIds: List<Long>,
        sort: String?
    ): Flowable<PagingData<File>> {
        TODO("Not yet implemented")
    }

    override fun updateFolderCover(id: Long, fileId: Long): Single<Folder> {
        TODO("Not yet implemented")
    }

    @VisibleForTesting
    fun addFolders(vararg folders: Folder) {
        for (folder in folders) {
            foldersServiceData.add(folder)
        }
    }

    @VisibleForTesting
    fun addFiles(folder: String, vararg files: File) {
        val fileList = filesServiceData[folder]
        if(fileList == null){
            filesServiceData[folder] = files.toMutableList()
        }else{
            fileList.addAll(files)
        }
    }
}