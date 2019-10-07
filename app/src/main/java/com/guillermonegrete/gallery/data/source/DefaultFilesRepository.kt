package com.guillermonegrete.gallery.data.source

class DefaultFilesRepository: FilesRepository {


    override fun getFolders(): List<String> {
        return listOf("Person", "Another", "Stuff")
    }

}