package com.guillermonegrete.gallery.files.source

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.guillermonegrete.gallery.data.FileResponse
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.data.source.local.AppDatabase
import com.guillermonegrete.gallery.data.source.remote.FilesServerAPI
import com.guillermonegrete.gallery.files.source.local.FileRemoteKey
import com.guillermonegrete.gallery.files.source.local.FileScopeCrossRef
import com.guillermonegrete.gallery.files.source.local.FileScopeHelper
import com.guillermonegrete.gallery.files.source.local.FileWithFolder
import com.guillermonegrete.gallery.files.source.local.FileTagCrossRef
import com.guillermonegrete.gallery.folders.source.local.FolderEntity
import com.guillermonegrete.gallery.tags.source.local.toFileEntity
import kotlinx.coroutines.rx3.await
import retrofit2.HttpException


@OptIn(ExperimentalPagingApi::class)
class
FileRemoteMediator(
    private val database: AppDatabase,
    private val apiService: FilesServerAPI,
    private val folderId: Long?,
    private val sort: String,
    private val tagIds: List<Long>?,
) : RemoteMediator<Int, FileWithFolder>() {

    private val remoteKeyId = FileScopeHelper.generateScopeId(folderId, sort, tagIds, tagIds)

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FileWithFolder>
    ): MediatorResult {
        return try {
            state.anchorPosition
            // 1. Calculate the exact page number we are about to request
            val page = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = database.fileRemoteKeyDao().getRemoteKeyById(remoteKeyId)
                    if (remoteKey != null && remoteKey.nextKey == null) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }

                    val nextKey = remoteKey?.nextKey ?: 0
                    nextKey
                }
            }

            // 2. Select the correct ETag based on which endpoint we are calling
            val usingGlobalToken = folderId == null && tagIds.isNullOrEmpty()
            val etagToSend = if (loadType == LoadType.REFRESH) {
                if (usingGlobalToken) {
                    // Endpoint 1 uses the same tag
                    database.fileRemoteKeyDao().getRemoteKeyById(GLOBAL_FILE_VERSION)?.etag
                } else {
                    // Endpoint 2 uses a different etag depending on the ids
                    database.fileRemoteKeyDao().getRemoteKeyById(remoteKeyId)?.etag
                }
            } else null

            val networkSingle = if (folderId == null) {
                apiService.getAllFiles(
                    folderTags = tagIds,
                    fileTags = tagIds,
                    page = page,
                    size = state.config.pageSize,
                    sort = sort,
                    ifNoneMatch = etagToSend,
                )
            } else {
                apiService.getFolderFiles(
                    folderId = folderId,
                    tagIds = tagIds,
                    page = page,
                    size = state.config.pageSize,
                    sort = sort,
                    ifNoneMatch = etagToSend,
                )
            }

            // 3. Fire API Call passing the page-specific ETag
            val response = networkSingle.await()

            // 4. Handle HTTP 304 Not Modified (This specific page slice is untouched)
            if (response.code() == 304) {
                return MediatorResult.Success(endOfPaginationReached = false)
            }

            if (!response.isSuccessful) {
                return MediatorResult.Error(HttpException(response))
            }

            // 5. Handle HTTP 200 OK (New data payload)
            val body = response.body() ?: return MediatorResult.Error(Exception("Empty body"))
            val newEtag = response.headers()["ETag"]
            val items = body.items
            val serverNextPage = body.nextPage

            database.withTransaction {
                // If refreshing the whole feed, clear out page index paths
                if (loadType == LoadType.REFRESH) {
                    if (etagToSend != null) {
                        database.fileDao().clearCrossRefsByEtag(etagToSend)
                        database.fileRemoteKeyDao().deleteKeysByEtag(etagToSend)
                        //database.fileDao().cleanupOrphanedFolders()
                    }
                }

                // Save the new token tied strictly to this page index row
                database.fileRemoteKeyDao().insertKey(
                    FileRemoteKey(id = remoteKeyId, nextKey = serverNextPage, etag = newEtag)
                )

                if (usingGlobalToken) {
                    database.fileRemoteKeyDao().insertKey(
                        FileRemoteKey(id = GLOBAL_FILE_VERSION, nextKey = null, etag = newEtag)
                    )
                }

                // Handle folders and files
                val embeddedFolders = items.mapNotNull { it.folder }.map { dto ->
                    FolderEntity(id = dto.id.toInt(), name = dto.name, coverUrl = dto.coverUrl, count = dto.count)
                }
                if (embeddedFolders.isNotEmpty()) {
                    database.folderDao().insertAll(embeddedFolders)
                }

                database.fileDao().insertAll(items.map(FileResponse::toEntity))
                database.fileDao()
                    .insertCrossRefs(items.map { FileScopeCrossRef(remoteKeyId, it.id) })

                // Tags
                val tags = items.flatMap { file -> file.tags }.map(Tag::toFileEntity)
                if (tags.isNotEmpty()) {
                    database.tagDao().insertAllTags(tags)
                }
                database.fileDao().insertAll(items.map(FileResponse::toEntity))
                val tagRefs = items.flatMap { fileDto ->
                    fileDto.tags.map { tagDto ->
                        FileTagCrossRef(fileId = fileDto.id, tagId = tagDto.id)
                    }
                }
                database.fileDao().insertTagRefs(tagRefs)
            }

            MediatorResult.Success(endOfPaginationReached = serverNextPage == null)

        } catch (exception: Exception) {
            MediatorResult.Error(exception)
        }
    }
}

private const val GLOBAL_FILE_VERSION = "global_file_version"
