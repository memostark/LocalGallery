package com.guillermonegrete.gallery.di

import com.guillermonegrete.gallery.folders.FoldersModule
import com.guillermonegrete.gallery.ViewModelBuilder
import com.guillermonegrete.gallery.files.FilesModule
import com.guillermonegrete.gallery.tags.TagsModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module(includes = [
    FilesModule::class,
    FoldersModule::class,
    TagsModule::class,
    RepositoryModule::class,
    ViewModelBuilder::class
])
object AggregatorModule
