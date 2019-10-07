package com.guillermonegrete.gallery.data.source

import androidx.annotation.VisibleForTesting

class FakeFilesRepository: FilesRepository {

    var filesServiceData = arrayListOf<String>()

    override fun getFolders(): List<String> {
        return filesServiceData
    }

    @VisibleForTesting
    fun addFolders(vararg folders: String) {
        for (folder in folders) {
            filesServiceData.add(folder)
        }
    }
}