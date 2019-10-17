package com.guillermonegrete.gallery.data.source

import androidx.annotation.VisibleForTesting
import io.reactivex.Single
import java.lang.RuntimeException

class FakeFilesRepository: FilesRepository {

    var filesServiceData = arrayListOf<String>()

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
        return Single.just(filesServiceData)
    }

    @VisibleForTesting
    fun addFolders(vararg folders: String) {
        for (folder in folders) {
            filesServiceData.add(folder)
        }
    }
}