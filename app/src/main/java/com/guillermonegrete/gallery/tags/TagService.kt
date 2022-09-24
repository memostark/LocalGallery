package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.data.Tag
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface TagService {

    @GET("/tags")
    fun getAllTags(): Single<Set<Tag>>

    @POST("/files/{id}/tags")
    fun addTag(@Path("id") id: Long, @Body tag: Tag): Single<Tag>

    @DELETE("/files/{fileId}/tags/{tagId}")
    fun deleteTagFromFile(@Path("fileId") fileId: Long, @Path("tagId") tagId: Long) : Completable
}
