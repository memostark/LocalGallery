package com.guillermonegrete.gallery.data.source.remote

import io.reactivex.Single
import retrofit2.http.GET

interface FilesServerAPI{

    @GET("folders")
    fun getFolders(): Single<List<Map<String, String>>>
}