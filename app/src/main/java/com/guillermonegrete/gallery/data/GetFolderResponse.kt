package com.guillermonegrete.gallery.data

data class GetFolderResponse(
    val name: String,
    val folders: List<Folder>
)