package com.guillermonegrete.gallery.folders.source

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.guillermonegrete.gallery.data.source.local.AppDatabase
import com.guillermonegrete.gallery.folders.source.local.FolderEntity
import com.guillermonegrete.gallery.folders.source.local.FolderRemoteKey
import kotlinx.coroutines.rx3.await

@OptIn(ExperimentalPagingApi::class)
class FolderRemoteMediator(
    private val database: AppDatabase,
    private val apiService: FoldersAPI,
) : RemoteMediator<Int, FolderEntity>() {

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
                    // Count only items currently loaded into memory right now
                    val itemsLoadedSoFar = state.pages.sumOf { it.data.size }

                    if (itemsLoadedSoFar == 0) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }

                    // Calculate current active screen page chunk
                    val currentPageLoaded = itemsLoadedSoFar / state.config.pageSize

                    // Look up next key from your existing table design
                    val remoteKey = database.folderRemoteKeyDao().getRemoteKeyForPage(currentPageLoaded)
                    remoteKey?.nextKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

            // 2. Fetch the ETag saved explicitly for THIS target page
            val targetPageEtag = database.folderRemoteKeyDao().getRemoteKeyForPage(page)?.eTag

            // 3. Fire API Call passing the page-specific ETag
            val response = apiService.getFoldersResponse(
                page = page,
                size = state.config.pageSize,
                ifNoneMatch = targetPageEtag // Sends e.g., "v2-p1-s20" if it exists
            ).await()

            // 4. Handle HTTP 304 Not Modified (This specific page slice is untouched)
            if (response.code() == 304) {
                return MediatorResult.Success(endOfPaginationReached = false)
            }

            // 5. Handle HTTP 200 OK (New data payload)
            val body = response.body() ?: return MediatorResult.Error(Exception("Empty body"))
            val newEtag = response.headers()["ETag"] // Extract the updated page-specific ETag
            val items = body.page.items
            val endOfPaginationReached = items.isEmpty()

            database.withTransaction {
                // If refreshing the whole feed, clear out page index paths
                if (loadType == LoadType.REFRESH) {
                    database.folderRemoteKeyDao().clearRemoteKeys()
                    database.folderDao().clearAllFolders()
                }

                val nextKey = if (endOfPaginationReached) null else page + 1

                // Save the new token tied strictly to this page index row
                database.folderRemoteKeyDao().insertKey(
                    FolderRemoteKey(pageNumber = page, eTag = newEtag, nextKey = nextKey)
                )

                database.folderDao().insertAll(items.map { it.toEntity() })
                Unit
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (exception: Exception) {
            MediatorResult.Error(exception)
        }
    }
}
