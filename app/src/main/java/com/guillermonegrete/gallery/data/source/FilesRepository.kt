package com.guillermonegrete.gallery.data.source

interface FilesRepository {

    fun getFolders(): List<String>
}