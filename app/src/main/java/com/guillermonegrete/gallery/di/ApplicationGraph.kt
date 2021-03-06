package com.guillermonegrete.gallery.di

import android.content.Context
import com.guillermonegrete.gallery.folders.FoldersListFragment
import com.guillermonegrete.gallery.folders.FoldersModule
import com.guillermonegrete.gallery.ViewModelBuilder
import com.guillermonegrete.gallery.files.FilesListFragment
import com.guillermonegrete.gallery.files.FilesModule
import com.guillermonegrete.gallery.files.details.FileDetailsFragment
import dagger.BindsInstance
import dagger.Component

@Component(modules = [
    FilesModule::class,
    FoldersModule::class,
    RepositoryModule::class,
    ViewModelBuilder::class
])
interface ApplicationGraph {

    @Component.Factory
    interface Factory {
        // With @BindsInstance, the Context passed in will be available in the graph
        fun create(@BindsInstance context: Context): ApplicationGraph
    }

    fun inject(fragment: FoldersListFragment)

    fun inject(fragment: FilesListFragment)

    fun inject(fragment: FileDetailsFragment)
}