package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.data.Tag
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TagService {

    @GET("/tags")
    fun getAllTags(): Single<Set<Tag>>

    @POST("/files/{id}/tags")
    fun addTag(@Path("id") id: Long, @Body tag: Tag): Single<Tag>
}
