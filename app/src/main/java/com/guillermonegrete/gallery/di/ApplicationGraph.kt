package com.guillermonegrete.gallery.di

import android.content.Context
import com.guillermonegrete.gallery.FoldersListFragment
import com.guillermonegrete.gallery.FoldersModule
import com.guillermonegrete.gallery.ViewModelBuilder
import com.guillermonegrete.gallery.files.FilesListFragment
import com.guillermonegrete.gallery.files.FilesModule
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
}