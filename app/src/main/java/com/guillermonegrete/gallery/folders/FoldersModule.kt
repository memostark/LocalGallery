package com.guillermonegrete.gallery.folders

import androidx.lifecycle.ViewModel
import com.guillermonegrete.gallery.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class FoldersModule {

    @Binds
    @IntoMap
    @ViewModelKey(FoldersViewModel::class)
    abstract fun bindsViewModel(viewModel: FoldersViewModel): ViewModel
}