package com.guillermonegrete.gallery.data.source.remote

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface FilesServerAPI{

    @GET("folders")
    fun getFolders(): Single<List<Map<String, String>>>

    @GET("folders/{path}")
    fun getFiles(@Path(value="path", encoded = false)  path: String): Single<List<String>>
}