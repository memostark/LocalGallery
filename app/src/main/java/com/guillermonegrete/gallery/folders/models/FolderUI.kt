package com.guillermonegrete.gallery.folders.models

import com.guillermonegrete.gallery.data.Folder

sealed class FolderUI {
    class Model(
        val name: String,
        val coverUrl: String,
        val count: Int
    ) : FolderUI() {
        constructor(folder: Folder) : this(folder.name, folder.coverUrl, folder.count){
            title = folder.title
        }

        var title: String? = null
    }

    class HeaderModel(val title: String) : FolderUI()
}
