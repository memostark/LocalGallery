package com.guillermonegrete.gallery.data.source

import io.reactivex.Single

interface FilesRepository {

    fun updateRepoURL(newURL: String)

    fun getFolders(): List<String>

    fun getObservableFolders(): Single<List<String>>
}