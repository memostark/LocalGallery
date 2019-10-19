package com.guillermonegrete.gallery.files

import androidx.lifecycle.ViewModel
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import io.reactivex.Single

class FilesViewModel(
    settings: SettingsRepository,
    private val filesRepository: FilesRepository
): ViewModel() {

    init {
        val url = settings.getServerURL()
        filesRepository.updateRepoURL(url)
    }

    fun loadFiles(folder: String): Single<List<String>>{
        return filesRepository.getFiles(folder)
    }
}