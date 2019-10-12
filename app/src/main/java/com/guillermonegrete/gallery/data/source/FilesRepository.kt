package com.guillermonegrete.gallery.data.source

interface FilesRepository {

    fun updateRepoURL(newURL: String)

    fun getFolders(): List<String>
}