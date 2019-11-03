package com.guillermonegrete.gallery.files

import androidx.lifecycle.ViewModel
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class FilesViewModel @Inject constructor(
    settings: SettingsRepository,
    private val filesRepository: FilesRepository
): ViewModel() {

    val loadingIndicator: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val networkError: BehaviorSubject<Boolean> = BehaviorSubject.create()

    val openFolder: Subject<File> = PublishSubject.create()

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

    fun openFilesDetails(file: File){
        openFolder.onNext(file)
    }
}