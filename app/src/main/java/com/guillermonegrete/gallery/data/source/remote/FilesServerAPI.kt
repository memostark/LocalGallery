package com.guillermonegrete.gallery.data.source.remote

import android.os.Parcelable
import com.guillermonegrete.gallery.data.FileResponse
import com.guillermonegrete.gallery.data.GetFolderResponse
import com.guillermonegrete.gallery.data.PagedFileResponse
import io.reactivex.rxjava3.core.Single
import kotlinx.parcelize.Parcelize
import retrofit2.http.Body
import retrofit2.http.GET
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
        @Body tagIds: List<Long>,
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

    @POST("files")
    fun getAllFilesByTags(
        @Body tags: FilterTags,
        @Query("page") page: Int,
        @Query("sort") sort: String? = null,
        @Query("size") size: Int = 30,
    ): Single<PagedFileResponse>
}

@Parcelize
data class FilterTags(
    val fileTagIds: List<Long> = emptyList(),
    val folderTagIds: List<Long> = emptyList()
) : Parcelable
