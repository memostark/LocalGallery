package com.guillermonegrete.gallery.di

import com.guillermonegrete.gallery.data.*
import com.guillermonegrete.gallery.data.source.DefaultFilesRepository
import com.guillermonegrete.gallery.data.source.DefaultSettingsRepository
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import com.guillermonegrete.gallery.data.source.remote.FilesServerAPI
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

@Module(includes = [RepositoryModuleBinds::class])
object RepositoryModule {

    @Provides
    fun provideFileServer(): FilesServerAPI{
        val moshi = Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .add(PolymorphicJsonAdapterFactory.of(FileResponse::class.java, "file_type")
                .withSubtype(ImageFileResponse::class.java, FileType.Image.name)
                .withSubtype(VideoFileResponse::class.java, FileType.Video.name))
            .add(KotlinJsonAdapterFactory())
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
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