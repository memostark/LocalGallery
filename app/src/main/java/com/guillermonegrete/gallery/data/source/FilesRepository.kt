package com.guillermonegrete.gallery.data.source

import io.reactivex.Single

interface FilesRepository {

    fun updateRepoURL(newURL: String)

    fun getFolders(): Single<List<String>>

    fun getFiles(folder: String): Single<List<String>>
}