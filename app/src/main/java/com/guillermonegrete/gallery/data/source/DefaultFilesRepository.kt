package com.guillermonegrete.gallery.data.source

import android.util.Patterns
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.guillermonegrete.gallery.data.source.remote.FilesServerAPI
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class DefaultFilesRepository: FilesRepository {

    private var fileAPI: FilesServerAPI
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        fileAPI = retrofit.create(FilesServerAPI::class.java)
    }

    override fun updateRepoURL(newURL: String) {
        val url = if(Patterns.WEB_URL.matcher(newURL).matches()) newURL else BASE_URL
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        fileAPI = retrofit.create(FilesServerAPI::class.java)
    }

    override fun getFolders(): List<String> {
        val mapList = fileAPI.getFolders()
            .subscribeOn(Schedulers.io())
            .blockingGet()
        return mapList.map { it["name"] ?: "" }
    }

    companion object{
        var BASE_URL = "http://localhost"
    }

}