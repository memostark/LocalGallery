package com.guillermonegrete.gallery.tags

import androidx.lifecycle.ViewModel
import com.guillermonegrete.gallery.ViewModelKey
import com.guillermonegrete.gallery.files.details.AddTagViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class TagsModule {

    @Binds
    @IntoMap
    @ViewModelKey(AddTagViewModel::class)
    abstract fun bindsViewModel(viewModel: AddTagViewModel): ViewModel
}
