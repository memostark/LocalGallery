package com.guillermonegrete.gallery.folders.source

import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import com.guillermonegrete.gallery.data.Folder
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class FoldersPageSource(
    private val foldersAPI: FoldersAPI,
    private val query: String?,
    private val sort: String?
): RxPagingSource<Int, Folder>()  {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, Folder>> {
        val nextPageNumber = params.key ?: 0
        return foldersAPI.getFolders(nextPageNumber, query, sort)
            .subscribeOn(Schedulers.io())
            .map { toLoadResult(it, nextPageNumber) }
            .onErrorReturn { LoadResult.Error(it) }
    }

    private fun toLoadResult(response: PagedFolderResponse, nextPageNumber: Int): LoadResult<Int, Folder> {
        val rawItems = response.page.items
        val items = if(nextPageNumber == 0 && rawItems.isNotEmpty()) rawItems.apply { first().title = response.name } else rawItems
        return LoadResult.Page(
            data = items,
            prevKey = if (nextPageNumber > 0) nextPageNumber - 1 else null,
            nextKey = if (nextPageNumber < response.page.totalItems - 1) nextPageNumber + 1 else null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Folder>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
