package com.guillermonegrete.gallery.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.guillermonegrete.gallery.data.source.DefaultFilesRepository
import com.guillermonegrete.gallery.data.source.DefaultSettingsRepository
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import com.guillermonegrete.gallery.data.source.remote.FilesServerAPI
import dagger.Binds
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module(includes = [RepositoryModuleBinds::class])
object RepositoryModule {

    @Provides
    fun provideFileServer(): FilesServerAPI{
        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        return retrofit.create(FilesServerAPI::class.java)
    }

}

@Module
abstract class RepositoryModuleBinds{
    @Binds
    abstract fun provideFilesRepository(repository: DefaultFilesRepository): FilesRepository

    @Binds
    abstract fun provideSettingsRepository(repository: DefaultSettingsRepository): SettingsRepository
}