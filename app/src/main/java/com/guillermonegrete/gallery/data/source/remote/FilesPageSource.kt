package com.guillermonegrete.gallery.data.source.remote

import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import com.guillermonegrete.gallery.data.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class FilesPageSource(
    private val filesApi: FilesServerAPI,
    private val baseUrl: String,
    private val folder: Folder,
    private val sort: String?,
    private val tagId: Long
): RxPagingSource<Int, File>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, File>> {
        val nextPageNumber = params.key ?: 0
        return getFilesSource(nextPageNumber)
            .subscribeOn(Schedulers.io())
            .map { toLoadResult(it, nextPageNumber) }
            .onErrorReturn { LoadResult.Error(it) }
    }

    /**
     * If folder is not empty, get files from endpoint /folder/{folder_name} or if a tag filter is set (tagId is not zero) then from /folder/{folderId}/tags/{tagId}
     * else get it from the files/ endpoint
     */
    private fun getFilesSource(nextPageNumber: Int): Single<PagedFileResponse> {
        return if(folder.name.isNotEmpty()) {
            if(tagId == 0L) filesApi.getPagedFiles(baseUrl, folder.name, nextPageNumber, sort) else filesApi.getPagedFilesByTag(folder.id, tagId, nextPageNumber, sort)
        } else {
            if(tagId == 0L) filesApi.getPagedFiles(baseUrl, nextPageNumber, sort) else filesApi.getAllFilesByTag(tagId, nextPageNumber, sort)
        }
    }

    private fun toLoadResult(response: PagedFileResponse, nextPageNumber: Int): LoadResult<Int, File> {
        return LoadResult.Page(
            data = response.items.map { fileResponse ->  println(fileResponse); fileResponse.toFile() },
            prevKey = if (nextPageNumber > 0) nextPageNumber - 1 else null,
            nextKey = if (nextPageNumber < response.totalPages - 1) nextPageNumber + 1 else null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, File>): Int? {
        // Try to find the page key of the closest page to anchorPosition, from
        // either the prevKey or the nextKey, but you need to handle nullability
        // here:
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey null -> anchorPage is the initial page, so
        //    just return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }

    }

}
