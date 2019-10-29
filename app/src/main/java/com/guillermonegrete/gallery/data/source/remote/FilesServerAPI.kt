package com.guillermonegrete.gallery.data.source.remote

import com.guillermonegrete.gallery.data.Folder
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface FilesServerAPI{

    @GET("folders")
    fun getFolders(): Single<List<Folder>>

    @GET("folders/{path}")
    fun getFiles(@Path(value="path", encoded = false)  path: String): Single<List<String>>
}