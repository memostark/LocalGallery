package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.data.Tag
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET

interface TagService {

    @GET("/tags")
    fun getAllTags(): Single<Set<Tag>>
}
