package com.guillermonegrete.gallery.folders

import androidx.lifecycle.ViewModel
import com.guillermonegrete.gallery.data.GetFolderResponse
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class FoldersViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val filesRepository: FilesRepository
): ViewModel() {

    val loadingIndicator: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val urlAvailable: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val networkError: Subject<Boolean> = PublishSubject.create()

    val rootFolderEmpty: Subject<Boolean> = PublishSubject.create()

    val openFolder: Subject<String> = PublishSubject.create()

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

    fun getFolders(): Single<GetFolderResponse>{
        loadingIndicator.onNext(true)
        val serverUrl = settings.getServerURL()

        return filesRepository.getFolders()
            .compose {
                // Check if has url to show appropriate layout and folders list
                if(serverUrl.isEmpty()) {
                    urlAvailable.onNext(false)
                    Single.just(GetFolderResponse("", emptyList()))
                } else {
                    urlAvailable.onNext(true)
                    it
                }
            }
            .doOnSuccess {
                if(it.folders.isEmpty()) rootFolderEmpty.onNext(true)
                loadingIndicator.onNext(false)
            }
            .doOnError{
                loadingIndicator.onNext(false)
                networkError.onNext(true)
            }
    }

    fun openFolder(folder: String){
        openFolder.onNext(folder)
    }
}