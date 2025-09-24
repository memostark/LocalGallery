package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.data.FileResponse
import com.guillermonegrete.gallery.data.Tag
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface TagService {

    @GET("/tags")
    fun getAllTags(): Single<Set<Tag>>

    @GET("folders/{id}/tags")
    fun getFolderTags(@Path("id") id: Long): Single<Set<Tag>>

    @POST("/files/{id}/tags")
    fun addTag(@Path("id") id: Long, @Body tag: Tag): Single<Tag>

    @POST("/tags/{id}/files")
    fun addTagToFiles(@Path("id") id: Long, @Body fileIds: List<Long>): Single<List<FileResponse>>

    @DELETE("/files/{fileId}/tags/{tagId}")
    fun deleteTagFromFile(@Path("fileId") fileId: Long, @Path("tagId") tagId: Long) : Completable

    @GET("/tags/folders")
    fun getFolderTags(): Single<Set<Tag>>
}
