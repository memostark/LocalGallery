package com.guillermonegrete.gallery.data.source

import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.GetFolderResponse
import io.reactivex.Single

interface FilesRepository {

    fun updateRepoURL(newURL: String)

    fun getFolders(): Single<GetFolderResponse>

    fun getFiles(folder: String): Single<List<File>>
}