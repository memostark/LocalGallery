package com.guillermonegrete.gallery.folders.source

import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import com.guillermonegrete.gallery.data.Folder
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class FoldersPageSource(
    private val foldersAPI: FoldersAPI,
    private val baseUrl: String,
    private val sort: String?
): RxPagingSource<Int, Folder>()  {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, Folder>> {
        val nextPageNumber = params.key ?: 0
        return foldersAPI.getFolders(baseUrl, nextPageNumber, sort)
            .subscribeOn(Schedulers.io())
            .map { toLoadResult(it, nextPageNumber) }
            .onErrorReturn { LoadResult.Error(it) }
    }

    private fun toLoadResult(response: PagedFolderResponse, nextPageNumber: Int): LoadResult<Int, Folder> {
        Timber.d("Paging data $response")
        return LoadResult.Page(
            data = response.page.items,
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
