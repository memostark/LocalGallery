package com.guillermonegrete.gallery.data.source

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.paging.rxjava3.flowable
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.GetFolderResponse
import com.guillermonegrete.gallery.data.source.local.AppDatabase
import com.guillermonegrete.gallery.data.source.remote.FilesServerAPI
import com.guillermonegrete.gallery.data.source.remote.FilterTags
import com.guillermonegrete.gallery.files.source.FileRemoteMediator
import com.guillermonegrete.gallery.files.source.local.FileScopeHelper
import com.guillermonegrete.gallery.files.source.local.FileWithFolder
import com.guillermonegrete.gallery.folders.source.FOLDER_PAGE_SIZE
import com.guillermonegrete.gallery.folders.source.FolderRemoteMediator
import com.guillermonegrete.gallery.folders.source.FoldersAPI
import com.guillermonegrete.gallery.folders.source.local.FolderScopeHelper
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class DefaultFilesRepository @Inject constructor(
    private val fileAPI: FilesServerAPI,
    private val foldersAPI: FoldersAPI,
    private val database: AppDatabase,
    private val cachePref: CachePreferenceManager,
): FilesRepository {

    override fun getFolders(): Single<GetFolderResponse> {
        return fileAPI.getFolders()
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getPagedFolders(tagIds: List<Long>, query: String?, sort: String): Flowable<PagingData<Folder>> {
        var titleAssigned: Boolean
        var title: String? = null
        return Pager(
            PagingConfig(pageSize = FOLDER_PAGE_SIZE),
            remoteMediator = FolderRemoteMediator(database, foldersAPI, cachePref, query, sort, tagIds)
        ) {
            val currentScopeId = FolderScopeHelper.generateId(query, sort, tagIds)
            title = cachePref.getFolderName()

            when (sort) {
                "name,desc"  -> database.folderDao().getFoldersSortedByNameDesc(currentScopeId)
                "count,asc"  -> database.folderDao().getFoldersSortedByCountAsc(currentScopeId)
                "count,desc" -> database.folderDao().getFoldersSortedByCountDesc(currentScopeId)
                else         -> database.folderDao().getFoldersSortedByNameAsc(currentScopeId)
            }
        }.flowable
            .map { pagingData ->
                pagingData.map { folderEntity ->
                    titleAssigned = false
                    folderEntity.toDomainModel().apply {
                        if (!titleAssigned) {
                            this.title = title
                            titleAssigned = true
                        }
                    }
                }
            }
    }

    override fun getFiles(folder: String): Single<List<File>> {
        return fileAPI.getFiles(folder).map { list ->
            // This map should not be necessary later because a Moshi adapter should handle the file object creation
            list.map { response -> println(response); response.toFile() }
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getPagedFiles(folder: Folder, tagIds: FilterTags, sort: String): Flowable<PagingData<File>> {
        val folderId = if (folder == Folder.NULL_FOLDER) null else folder.id
        return Pager(
            PagingConfig(pageSize = 20),
            remoteMediator = FileRemoteMediator(database, fileAPI, folderId, sort, tagIds.fileTagIds)
        ) {
            val currentScopeId = FileScopeHelper.generateScopeId(
                folderId,
                sort,
                tagIds.folderTagIds,
                tagIds.fileTagIds
            )

            val fileDao = database.fileDao()
            when (sort) {
                "filename,desc" -> fileDao.getFilesByNameDesc(currentScopeId)
                "filename,asc" -> fileDao.getFilesByNameAsc(currentScopeId)
                "creationDate,desc" -> fileDao.getFilesByCreatedDesc(currentScopeId)
                "creationDate,asc" -> fileDao.getFilesByCreatedAsc(currentScopeId)
                "lastModified,asc" -> fileDao.getFilesByModifiedAsc(currentScopeId)
                else -> fileDao.getFilesByModifiedDesc(currentScopeId)
            }
        }.flowable
            .map { pagingData ->
                pagingData.map(FileWithFolder::toDomainModel)
            }
    }

    override fun updateFolderCover(id: Long, fileId: Long) = foldersAPI.updateFolderCover(id, fileId)

    override fun getFilesInfo() = fileAPI.getFilesInfo()

}