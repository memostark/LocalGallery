package com.guillermonegrete.gallery.di

import com.guillermonegrete.gallery.BuildConfig
import com.guillermonegrete.gallery.common.HostSelectionInterceptor
import com.guillermonegrete.gallery.data.*
import com.guillermonegrete.gallery.data.source.DefaultFilesRepository
import com.guillermonegrete.gallery.data.source.DefaultSettingsRepository
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import com.guillermonegrete.gallery.data.source.remote.FilesServerAPI
import com.guillermonegrete.gallery.folders.source.FoldersAPI
import com.guillermonegrete.gallery.tags.DefaultTagRepository
import com.guillermonegrete.gallery.tags.TagRepository
import com.guillermonegrete.gallery.tags.TagService
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

@Module(includes = [RepositoryModuleBinds::class])
object RepositoryModule {

    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit{
        val moshi = Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .add(PolymorphicJsonAdapterFactory.of(FileResponse::class.java, "file_type")
                .withSubtype(ImageFileResponse::class.java, FileType.Image.name)
                .withSubtype(VideoFileResponse::class.java, FileType.Video.name))
            .add(KotlinJsonAdapterFactory())
            .build()
        return Retrofit.Builder()
            .baseUrl("http://localhost/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    }

    @Provides
    fun provideOkHttp(settingsRepository: SettingsRepository): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(HostSelectionInterceptor(settingsRepository))

        if(BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            clientBuilder.addInterceptor(interceptor)
        }
        return clientBuilder.build()
    }

    @Provides
    fun provideFileServer(retrofit: Retrofit): FilesServerAPI = retrofit.create(FilesServerAPI::class.java)

    @Provides
    fun provideFolderAPI(retrofit: Retrofit): FoldersAPI = retrofit.create(FoldersAPI::class.java)

    @Provides
    fun provideTagService(retrofit: Retrofit): TagService = retrofit.create(TagService::class.java)

}

@Module
abstract class RepositoryModuleBinds{
    @Binds
    abstract fun provideFilesRepository(repository: DefaultFilesRepository): FilesRepository

    @Binds
    abstract fun provideSettingsRepository(repository: DefaultSettingsRepository): SettingsRepository

    @Binds
    abstract fun provideTagsRepository(repository: DefaultTagRepository): TagRepository
}
