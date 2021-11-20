package com.guillermonegrete.gallery.folders.source

import com.guillermonegrete.gallery.data.Folder

data class PagedFolderResponse(
    val name: String,
    val page: FolderPage,

)

data class FolderPage(
    val items: List<Folder>,
    val totalPages: Int,
    val totalItems: Int
)
