package com.guillermonegrete.gallery.files

import androidx.lifecycle.ViewModel
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

class FilesViewModel(
    settings: SettingsRepository,
    private val filesRepository: FilesRepository
): ViewModel() {

    val loadingIndicator: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val networkError: BehaviorSubject<Boolean> = BehaviorSubject.create()

    init {
        val url = settings.getServerURL()
        filesRepository.updateRepoURL(url)
    }

    fun loadFiles(folder: String): Single<List<String>>{
        loadingIndicator.onNext(true)

        return filesRepository.getFiles(folder)
            .doOnSuccess { loadingIndicator.onNext(false) }
            .doOnError{
                loadingIndicator.onNext(false)
                networkError.onNext(true)
            }
    }
}