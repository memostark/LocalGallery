package com.guillermonegrete.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.guillermonegrete.gallery.data.source.DefaultSettingsRepository
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.files.FilesViewModel

/**
 * Factory for all ViewModels.
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val settingsRepository: DefaultSettingsRepository,
    private val filesRepository: FilesRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(FoldersViewModel::class.java) ->
                    FoldersViewModel(settingsRepository, filesRepository)
                isAssignableFrom(FilesViewModel::class.java) ->
                    FilesViewModel(settingsRepository, filesRepository)
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}
