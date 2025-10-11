package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.data.FileResponse
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.Tag
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface TagService {

    @GET("/tags")
    fun getAllTags(): Single<Set<Tag>>

    @GET("folders/{folderId}/tags")
    fun getTags(@Path("folderId") folderId: Long): Single<Set<Tag>>

    @POST("/files/{id}/tags")
    fun addTag(@Path("id") id: Long, @Body tag: Tag): Single<Tag>

    @POST("/folders/{id}/tags")
    fun addFolderTag(@Path("id") id: Long, @Body tag: Tag): Single<Tag>

    @POST("/tags/{id}/files")
    fun addTagToFiles(@Path("id") id: Long, @Body fileIds: List<Long>): Single<List<FileResponse>>

    @POST("/tags/{id}/folders")
    fun addTagToFolders(@Path("id") id: Long, @Body folderIds: List<Long>): Single<List<Folder>>

    @DELETE("/files/{fileId}/tags/{tagId}")
    fun deleteTagFromFile(@Path("fileId") fileId: Long, @Path("tagId") tagId: Long) : Completable

    @DELETE("/folders/{folderId}/tags/{tagId}")
    fun deleteTagFromFolder(@Path("folderId") folderId: Long, @Path("tagId") tagId: Long) : Completable

    @GET("/tags/files")
    fun getFileTags(): Single<Set<Tag>>

    @GET("/tags/folders")
    fun getFolderTags(): Single<Set<Tag>>

    @GET("/tags/folders/{folderId}")
    fun getFolderTags(@Path("folderId") folderId: Long): Single<Set<Tag>>
}
