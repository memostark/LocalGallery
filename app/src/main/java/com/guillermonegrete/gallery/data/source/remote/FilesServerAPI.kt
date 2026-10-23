package com.guillermonegrete.gallery.data.source.remote

import android.os.Parcelable
import com.guillermonegrete.gallery.data.FileInfoResponse
import com.guillermonegrete.gallery.data.FileResponse
import com.guillermonegrete.gallery.data.GetFolderResponse
import com.guillermonegrete.gallery.data.PagedFileResponse
import io.reactivex.rxjava3.core.Single
import kotlinx.parcelize.Parcelize
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface FilesServerAPI{

    @GET("folders")
    fun getFolders(): Single<GetFolderResponse>

    @GET("folders/{folderName}")
    fun getFiles(
        @Path(value="folderName", encoded = true) folderName: String
    ): Single<List<FileResponse>>

    @GET("folders/{folderId}/files")
    fun getFolderFiles(
        @Path(value="folderId") folderId: Long,
        @Header("If-None-Match") ifNoneMatch: String?,
        @Query("tagIds") tagIds: List<Long>?,
        @Query("page") page: Int,
        @Query("sort") sort: String? = null,
        @Query("size") size: Int = 30,
    ): Single<Response<PagedFileResponse>>

    @GET("files")
    fun getAllFiles(
        @Header("If-None-Match") ifNoneMatch: String?,
        @Query("fileTagIds") fileTags: List<Long>?,
        @Query("folderTagIds") folderTags: List<Long>?,
        @Query("page") page: Int,
        @Query("sort") sort: String? = null,
        @Query("size") size: Int = 30,
    ): Single<Response<PagedFileResponse>>

    @GET("files/info")
    fun getFilesInfo(): Single<FileInfoResponse>
}

@Parcelize
data class FilterTags(
    val fileTagIds: List<Long> = emptyList(),
    val folderTagIds: List<Long> = emptyList()
) : Parcelable
