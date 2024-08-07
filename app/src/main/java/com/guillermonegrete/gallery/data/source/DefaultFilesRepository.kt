package com.guillermonegrete.gallery.data.source

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava3.flowable
import com.guillermonegrete.gallery.data.*
import com.guillermonegrete.gallery.data.source.remote.FilesPageSource
import com.guillermonegrete.gallery.data.source.remote.FilesServerAPI
import com.guillermonegrete.gallery.folders.source.FoldersAPI
import com.guillermonegrete.gallery.folders.source.FoldersPageSource
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class DefaultFilesRepository @Inject constructor(
    private val fileAPI: FilesServerAPI,
    private val foldersAPI: FoldersAPI
): FilesRepository {

    override fun getFolders(): Single<GetFolderResponse> {
        return fileAPI.getFolders()
    }

    override fun getPagedFolders(query: String?, sort: String?): Flowable<PagingData<Folder>> {
        return Pager(PagingConfig(pageSize = 20)) {
            FoldersPageSource(foldersAPI, query, sort)
        }.flowable
    }

    override fun getFiles(folder: String): Single<List<File>> {
        return fileAPI.getFiles(folder).map { list ->
            // This map should not be necessary later because a Moshi adapter should handle the file object creation
            list.map { response -> println(response); response.toFile() }
        }
    }

    override fun getPagedFiles(folder: Folder, tagId: Long, sort: String?): Flowable<PagingData<File>> {
        return Pager(PagingConfig(pageSize = 20)) {
            FilesPageSource(fileAPI, folder, sort, tagId)
        }.flowable
    }

    override fun updateFolderCover(id: Long, fileId: Long) = foldersAPI.updateFolderCover(id, fileId)

}