package com.guillermonegrete.gallery.folders.source

import com.guillermonegrete.gallery.data.Folder
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface FoldersAPI {

    @GET("folders")
    fun getFolders(
        @Query("page") page: Int,
        @Query("query") query: String? = null,
        @Query("sort") sort: String? = null,
        @Query("size") size: Int = 30,
    ): Single<PagedFolderResponse>

    @PATCH("folder/{id}/cover/{fileId}")
    fun updateFolderCover(
        @Path(value="id") id: Long,
        @Path(value="fileId") fileId: Long,
    ): Single<Folder>
}
