package com.guillermonegrete.gallery.data.source.remote

import com.guillermonegrete.gallery.data.FileResponse
import com.guillermonegrete.gallery.data.GetFolderResponse
import com.guillermonegrete.gallery.data.PagedFileResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FilesServerAPI{

    @GET("{baseUrl}folders")
    fun getFolders(@Path(value="baseUrl", encoded = true)  baseUrl: String): Single<GetFolderResponse>

    @GET("{baseUrl}folders/{path}")
    fun getFiles(
        @Path(value="baseUrl", encoded = true)  baseUrl: String,
        @Path(value="path", encoded = false)  path: String
    ): Single<List<FileResponse>>

    @GET("{baseUrl}folders/{path}")
    fun getPagedFiles(
        @Path(value="baseUrl", encoded = true)  baseUrl: String,
        @Path(value="path", encoded = false)  path: String,
        @Query("page") page: Int,
        @Query("sort") sort: String? = null,
        @Query("size") size: Int = 30,
    ): Single<PagedFileResponse>

    @GET("{baseUrl}files")
    fun getPagedFiles(
        @Path(value="baseUrl", encoded = true)  baseUrl: String,
        @Query("page") page: Int,
        @Query("sort") sort: String? = null,
        @Query("size") size: Int = 30,
    ): Single<PagedFileResponse>

    @GET("folders/{folderId}/tags/{tagId}")
    fun getPagedFilesByTag(
        @Path(value="folderId") folderId: Long,
        @Path(value="tagId") tagId: Long,
        @Query("page") page: Int,
        @Query("sort") sort: String? = null,
        @Query("size") size: Int = 30,
    ): Single<PagedFileResponse>
}
