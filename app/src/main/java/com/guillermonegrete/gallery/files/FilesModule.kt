package com.guillermonegrete.gallery.files

import androidx.lifecycle.ViewModel
import com.guillermonegrete.gallery.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class FilesModule {

    @Binds
    @IntoMap
    @ViewModelKey(FilesViewModel::class)
    abstract fun bindsViewModel(viewModel: FilesViewModel): ViewModel
}