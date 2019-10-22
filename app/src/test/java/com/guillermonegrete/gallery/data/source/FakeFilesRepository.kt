package com.guillermonegrete.gallery.data.source

import androidx.annotation.VisibleForTesting
import io.reactivex.Single
import java.lang.RuntimeException

class FakeFilesRepository: FilesRepository {

    var foldersServiceData = arrayListOf<String>()

    var filesServiceData: LinkedHashMap<String, MutableList<String>> = LinkedHashMap()

    var repoUrl = ""

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override fun updateRepoURL(newURL: String) {
        repoUrl = newURL
    }

    override fun getFolders(): Single<List<String>> {
        if(shouldReturnError) return Single.error(RuntimeException())
        return Single.just(foldersServiceData)
    }

    override fun getFiles(folder: String): Single<List<String>> {
        if(shouldReturnError) return Single.error(RuntimeException())
        return Single.just(filesServiceData[folder])
    }

    @VisibleForTesting
    fun addFolders(vararg folders: String) {
        for (folder in folders) {
            foldersServiceData.add(folder)
        }
    }

    @VisibleForTesting
    fun addFiles(folder: String, vararg files: String) {
        val fileList = filesServiceData[folder]
        if(fileList == null){
            filesServiceData[folder] = files.toMutableList()
        }else{
            fileList.addAll(files)
        }
    }
}