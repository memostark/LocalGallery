package com.guillermonegrete.gallery.folders.source

import com.guillermonegrete.gallery.data.Folder
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PagedFolderResponse(
    val name: String,
    val page: FolderPage,
)

@JsonClass(generateAdapter = true)
data class FolderPage(
    val items: List<Folder>,
    val totalPages: Int,
    val totalItems: Int,
    val nextPage: Int?,
)
