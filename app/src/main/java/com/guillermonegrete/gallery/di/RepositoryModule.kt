package com.guillermonegrete.gallery.di

import com.guillermonegrete.gallery.data.source.DefaultFilesRepository
import com.guillermonegrete.gallery.data.source.DefaultSettingsRepository
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import dagger.Binds
import dagger.Module

@Module
abstract class RepositoryModule {

    @Binds
    abstract fun provideFilesRepository(repository: DefaultFilesRepository): FilesRepository

    @Binds
    abstract fun provideSettingsRepository(repository: DefaultSettingsRepository): SettingsRepository
}