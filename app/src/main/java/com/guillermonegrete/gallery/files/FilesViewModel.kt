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

    val openDetails: Subject<Int> = PublishSubject.create()

    var cachedFileList = emptyList<File>()
        private set

    init {
        val url = settings.getServerURL()
        filesRepository.updateRepoURL(url)
    }

    fun loadFiles(folder: String): Single<List<File>> {
        loadingIndicator.onNext(true)

        return filesRepository.getFiles(folder)
            // This map should not be necessary later because Moshi adapter should handle the file object creation
            .map {list->
                list.map {
                    val type = it.split(".").last()
                    File(it, type)
                }
            }
            .doOnSuccess {
                loadingIndicator.onNext(false)
                cachedFileList = it
            }
            .doOnError{
                loadingIndicator.onNext(false)
                networkError.onNext(true)
            }
    }

    fun openFilesDetails(index: Int){
        openDetails.onNext(index)
    }
}