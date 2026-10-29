package com.guillermonegrete.gallery.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PagedFileResponse(
    val items: List<FileResponse>,
    val totalPages: Int,
    val totalItems: Int,
    val nextPage: Int?,
)
