package com.guillermonegrete.gallery.folders.source

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.guillermonegrete.gallery.data.source.CachePreferenceManager
import com.guillermonegrete.gallery.data.source.local.AppDatabase
import com.guillermonegrete.gallery.folders.source.local.FolderEntity
import com.guillermonegrete.gallery.folders.source.local.FolderRemoteKey
import com.guillermonegrete.gallery.folders.source.local.FolderScopeHelper
import kotlinx.coroutines.rx3.await
import retrofit2.HttpException

@OptIn(ExperimentalPagingApi::class)
class FolderRemoteMediator(
    private val database: AppDatabase,
    private val apiService: FoldersAPI,
    private val cachePrefs: CachePreferenceManager,
    private val query: String?,
    private val sort: String,
    private val tagIds: List<Long>?,
) : RemoteMediator<Int, FolderEntity>() {

    private val remoteKeyId = FolderScopeHelper.generateId(query, sort, tagIds)

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FolderEntity>
    ): MediatorResult {
        return try {
            state.anchorPosition
            // 1. Calculate the exact page number we are about to request
            val page = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = database.folderRemoteKeyDao().getRemoteKeyById(remoteKeyId)
                    if (remoteKey != null && remoteKey.nextKey == null) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }

                    val nextKey = remoteKey?.nextKey ?: 0
                    nextKey
                }
            }

            // 2. Fetch the ETag only when refreshing, otherwise send null so the backend actually return data instead of a 304 response
            val targetPageEtag = if (loadType == LoadType.REFRESH)
                cachePrefs.getGlobalFolderEtag()
            else null

            val networkSingle = if (tagIds == null) {
                apiService.getFoldersResponse(
                    page = page,
                    size = state.config.pageSize,
                    query = query,
                    sort = sort,
                    ifNoneMatch = targetPageEtag
                )
            } else {
                apiService.getPagedFoldersByTags(
                    tagIds = tagIds,
                    page = page,
                    size = state.config.pageSize,
                    query = query,
                    sort = sort,
                    ifNoneMatch = targetPageEtag
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
            val items = body.page.items
            val serverNextPage = body.page.nextPage

            database.withTransaction {
                // If refreshing the whole feed, clear out page index paths
                if (loadType == LoadType.REFRESH) {
                    database.folderRemoteKeyDao().clearRemoteKeys()
                    database.folderDao().clearAllFolders()
                }

                // Save the new token tied strictly to this page index row
                database.folderRemoteKeyDao().insertKey(
                    FolderRemoteKey(id = remoteKeyId, nextKey = serverNextPage)
                )

                database.folderDao().insertAll(items.map { it.toEntity(remoteKeyId) })
            }

            if (loadType == LoadType.REFRESH) {
                cachePrefs.saveGlobalFolderEtag(newEtag)
            }

            MediatorResult.Success(endOfPaginationReached = serverNextPage == null)

        } catch (exception: Exception) {
            MediatorResult.Error(exception)
        }
    }
}
