package com.guillermonegrete.gallery.data.source.remote

import androidx.paging.rxjava2.RxPagingSource
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.PagedFileResponse
import com.guillermonegrete.gallery.data.source.FilesRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class FilesPageSource(private val repository: FilesRepository, val folder: String): RxPagingSource<Int, File>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, File>> {
        val nextPageNumber = params.key ?: 0
        return repository.getPagedFiles(folder, nextPageNumber)
            .subscribeOn(Schedulers.io())
            .map { toLoadResult(it, nextPageNumber) }
            .onErrorReturn { LoadResult.Error(it) }
//            .onErrorReturn(::LoadResult.Error)

    }

    private fun toLoadResult(response: PagedFileResponse, nextPageNumber: Int): LoadResult<Int, File> {
        return LoadResult.Page(
            data = response.items.map {
                val type = it.url.split(".").last()
                File(it.url, type, it.width, it.height)
            },
            prevKey = if (nextPageNumber > 0) nextPageNumber - 1 else null,
            nextKey = if (nextPageNumber < response.totalPages) nextPageNumber + 1 else null
        )
    }

}