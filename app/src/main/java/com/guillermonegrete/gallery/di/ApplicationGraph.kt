package com.guillermonegrete.gallery.di

import com.guillermonegrete.gallery.folders.FoldersListFragment
import com.guillermonegrete.gallery.folders.FoldersModule
import com.guillermonegrete.gallery.ViewModelBuilder
import com.guillermonegrete.gallery.common.SortingDialog
import com.guillermonegrete.gallery.files.FilesListFragment
import com.guillermonegrete.gallery.files.FilesModule
import com.guillermonegrete.gallery.files.details.AddTagFragment
import com.guillermonegrete.gallery.files.details.FileDetailsFragment
import com.guillermonegrete.gallery.tags.TagsModule
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@EntryPoint
interface ApplicationGraph {

    fun inject(fragment: FoldersListFragment)

    fun inject(fragment: FilesListFragment)

    fun inject(fragment: FileDetailsFragment)

    fun inject(fragment: AddTagFragment)

    fun inject(fragment: SortingDialog)
}

@InstallIn(SingletonComponent::class)
@Module(includes = [
    FilesModule::class,
    FoldersModule::class,
    TagsModule::class,
    RepositoryModule::class,
    ViewModelBuilder::class
])
object AggregatorModule {}
