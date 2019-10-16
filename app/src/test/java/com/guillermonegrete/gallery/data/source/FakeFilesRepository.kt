package com.guillermonegrete.gallery.data.source

import androidx.annotation.VisibleForTesting
import io.reactivex.Single

class FakeFilesRepository: FilesRepository {

    var filesServiceData = arrayListOf<String>()

    var repoUrl = ""

    override fun updateRepoURL(newURL: String) {
        repoUrl = newURL
    }

    override fun getFolders(): List<String> {
        return filesServiceData
    }

    override fun getObservableFolders(): Single<List<String>> {
        return Single.just(filesServiceData)
    }

    @VisibleForTesting
    fun addFolders(vararg folders: String) {
        for (folder in folders) {
            filesServiceData.add(folder)
        }
    }
}