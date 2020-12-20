package com.guillermonegrete.gallery.data.source

import androidx.annotation.VisibleForTesting
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.GetFolderResponse
import io.reactivex.Single
import java.lang.RuntimeException

class FakeFilesRepository: FilesRepository {

    var foldersServiceData = arrayListOf<Folder>()

    var filesServiceData: LinkedHashMap<String, MutableList<File>> = LinkedHashMap()

    var repoUrl = ""

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override fun updateRepoURL(newURL: String) {
        repoUrl = newURL
    }

    override fun getFolders(): Single<GetFolderResponse> {
        if(shouldReturnError) return Single.error(RuntimeException())
        return Single.just(GetFolderResponse("Name", foldersServiceData))
    }

    override fun getFiles(folder: String): Single<List<File>> {
        if(shouldReturnError) return Single.error(RuntimeException())
        return Single.just(filesServiceData[folder])
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