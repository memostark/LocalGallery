package com.guillermonegrete.gallery.folders.source

import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface FoldersAPI {

    @GET("folders")
    fun getFolders(
        @Query("page") page: Int,
        @Query("query") query: String? = null,
        @Query("sort") sort: String? = null,
        @Query("size") size: Int = 30,
    ): Single<PagedFolderResponse>
}
