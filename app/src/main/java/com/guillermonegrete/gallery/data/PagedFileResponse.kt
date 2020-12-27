package com.guillermonegrete.gallery.data

data class PagedFileResponse(
    val items: List<FileResponse>,
    val totalPages: Int,
    val totalItems: Int,
)
