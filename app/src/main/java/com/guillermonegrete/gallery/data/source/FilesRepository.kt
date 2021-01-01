package com.guillermonegrete.gallery.data.source

import androidx.paging.PagingData
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.GetFolderResponse
import io.reactivex.Flowable
import io.reactivex.Single

interface FilesRepository {

    fun updateRepoURL(newURL: String)

    fun getFolders(): Single<GetFolderResponse>

    fun getFiles(folder: String): Single<List<File>>

    fun getPagedFiles(folder: String): Flowable<PagingData<File>>
}