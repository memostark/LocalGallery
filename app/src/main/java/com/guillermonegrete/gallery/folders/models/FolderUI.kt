package com.guillermonegrete.gallery.folders.models

import com.guillermonegrete.gallery.data.Folder

sealed class FolderUI {
    data class Model(
        val name: String,
        var coverUrl: String,
        val count: Int,
        val id: Long,
    ) : FolderUI() {
        constructor(folder: Folder) : this(folder.name, folder.coverUrl, folder.count, folder.id){
            title = folder.title
        }

        var title: String? = null
    }

    data class HeaderModel(val title: String) : FolderUI()
}
