package com.guillermonegrete.gallery.data.source

import androidx.paging.PagingData
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.GetFolderResponse
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

interface FilesRepository {

    fun getFolders(): Single<GetFolderResponse>

    fun getPagedFolders(tagIds: List<Long>, query: String?, sort: String?): Flowable<PagingData<Folder>>

    fun getFiles(folder: String): Single<List<File>>

    fun getPagedFiles(folder: Folder, tagIds: List<Long>, sort: String?): Flowable<PagingData<File>>

    fun updateFolderCover(id: Long, fileId: Long): Single<Folder>
}
