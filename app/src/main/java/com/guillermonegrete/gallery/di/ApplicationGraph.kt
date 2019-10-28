package com.guillermonegrete.gallery.di

import android.content.Context
import com.guillermonegrete.gallery.FoldersListFragment
import com.guillermonegrete.gallery.files.FilesListFragment
import dagger.BindsInstance
import dagger.Component

@Component
interface ApplicationGraph {

    @Component.Factory
    interface Factory {
        // With @BindsInstance, the Context passed in will be available in the graph
        fun create(@BindsInstance context: Context): ApplicationGraph
    }

    fun inject(fragment: FoldersListFragment)

    fun inject(fragment: FilesListFragment)
}