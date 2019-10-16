package com.guillermonegrete.gallery

import androidx.lifecycle.ViewModel
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

class FoldersViewModel(
    private val settings: SettingsRepository,
    private val filesRepository: FilesRepository
): ViewModel() {

    val loadingIndicator: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val urlAvailable: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    init {
        val url = settings.getServerURL()
        filesRepository.updateRepoURL(url)
    }

    fun getDialogData(): Single<String>{
        return settings.getServerUrl()
    }

    fun updateServerUrl(url: String) {
        filesRepository.updateRepoURL(url)
        settings.saveServerURL(url)
    }

    fun getFolders(): Single<List<String>>{
        loadingIndicator.onNext(true)
        val serverUrl = settings.getServerURL()

        return filesRepository.getFolders()
            .compose {
                // Check if has url to show appropriate layout and folders list
                if(serverUrl.isEmpty()) {
                    urlAvailable.onNext(false)
                    Single.just(emptyList())
                } else {
                    urlAvailable.onNext(true)
                    it
                }
            }
            .doOnSuccess { loadingIndicator.onNext(false) }
            .doOnError{ loadingIndicator.onNext(false) }
    }
}