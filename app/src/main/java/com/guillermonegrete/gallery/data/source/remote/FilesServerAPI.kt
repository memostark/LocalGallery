package com.guillermonegrete.gallery.data.source.remote

import com.guillermonegrete.gallery.data.FileResponse
import com.guillermonegrete.gallery.data.GetFolderResponse
import com.guillermonegrete.gallery.data.PagedFileResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FilesServerAPI{

    @GET("folders")
    fun getFolders(): Single<GetFolderResponse>

    @GET("folders/{folderName}")
    fun getFiles(
        @Path(value="folderName", encoded = false) folderName: String
    ): Single<List<FileResponse>>

    @GET("folders/{folderName}")
    fun getPagedFiles(
        @Path(value="folderName", encoded = false) folderName: String,
        @Query("page") page: Int,
        @Query("sort") sort: String? = null,
        @Query("size") size: Int = 30,
    ): Single<PagedFileResponse>

    @POST("folders/{folderId}/files")
    fun getPagedFilesByTag(
        @Path(value="folderId") folderId: Long,
        @Body fileIds: List<Long>,
        @Query("page") page: Int,
        @Query("sort") sort: String? = null,
        @Query("size") size: Int = 30,
    ): Single<PagedFileResponse>

    @GET("files")
    fun getAllFiles(
        @Query("page") page: Int,
        @Query("sort") sort: String? = null,
        @Query("size") size: Int = 30,
    ): Single<PagedFileResponse>

    @POST("tags/files")
    fun getAllFilesByTag(
        @Body fileIds: List<Long>,
        @Query("page") page: Int,
        @Query("sort") sort: String? = null,
        @Query("size") size: Int = 30,
    ): Single<PagedFileResponse>
}
