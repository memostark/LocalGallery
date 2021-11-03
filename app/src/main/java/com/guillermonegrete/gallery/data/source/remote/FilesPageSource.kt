package com.guillermonegrete.gallery.data.source.remote

import androidx.paging.rxjava2.RxPagingSource
import com.guillermonegrete.gallery.data.*
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class FilesPageSource(
    private val filesApi: FilesServerAPI,
    private val baseUrl: String,
    private val folder: String,
    private val sort: String?
): RxPagingSource<Int, File>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, File>> {
        val nextPageNumber = params.key ?: 0
        return getFilesSource(nextPageNumber)
            .subscribeOn(Schedulers.io())
            .map { toLoadResult(it, nextPageNumber) }
            .onErrorReturn { LoadResult.Error(it) }
    }

    /**
     * If folder is not empty, get files from endpoint url/folder/{folder_name}
     * else get it from the files/ endpoint
     */
    private fun getFilesSource(nextPageNumber: Int): Single<PagedFileResponse> {
        return if(folder.isNotEmpty())
            filesApi.getPagedFiles(baseUrl, folder, nextPageNumber, sort) else filesApi.getPagedFiles(baseUrl, nextPageNumber, sort)
    }

    private fun toLoadResult(response: PagedFileResponse, nextPageNumber: Int): LoadResult<Int, File> {
        return LoadResult.Page(
            data = response.items.map { fileResponse -> fileResponse.toFile() },
            prevKey = if (nextPageNumber > 0) nextPageNumber - 1 else null,
            nextKey = if (nextPageNumber < response.totalPages - 1) nextPageNumber + 1 else null
        )
    }

}