package com.guillermonegrete.gallery.data.source

import com.guillermonegrete.gallery.data.Folder
import io.reactivex.Single

interface FilesRepository {

    fun updateRepoURL(newURL: String)

    fun getFolders(): Single<List<Folder>>

    fun getFiles(folder: String): Single<List<String>>
}