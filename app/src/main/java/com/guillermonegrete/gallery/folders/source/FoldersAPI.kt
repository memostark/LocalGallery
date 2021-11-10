package com.guillermonegrete.gallery.folders.source

import com.guillermonegrete.gallery.data.GetFolderResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FoldersAPI {

    @GET("{baseUrl}folders")
    fun getFolders(@Path(value="baseUrl", encoded = true)  baseUrl: String): Single<GetFolderResponse>

    @GET("{baseUrl}folders")
    fun getFolders(
        @Path(value = "baseUrl", encoded = true) baseUrl: String,
        @Query("page") page: Int,
        @Query("sort") sort: String? = null,
        @Query("size") size: Int = 30,
    ): Single<PagedFolderResponse>
}
