package com.guillermonegrete.gallery

import androidx.lifecycle.ViewModel
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