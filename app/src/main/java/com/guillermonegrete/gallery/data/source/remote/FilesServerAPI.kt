package com.guillermonegrete.gallery.data.source.remote

import com.guillermonegrete.gallery.data.GetFolderResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface FilesServerAPI{

    @GET("{baseUrl}folders")
    fun getFolders(@Path(value="baseUrl", encoded = true)  baseUrl: String): Single<GetFolderResponse>

    @GET("{baseUrl}folders/{path}")
    fun getFiles(
        @Path(value="baseUrl", encoded = true)  baseUrl: String,
        @Path(value="path", encoded = false)  path: String
    ): Single<List<String>>
}