package com.guillermonegrete.gallery.folders

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.GetFolderResponse
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import javax.inject.Inject

class FoldersViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val filesRepository: FilesRepository
): ViewModel() {

    val loadingIndicator: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val urlAvailable: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val networkError: Subject<Boolean> = BehaviorSubject.createDefault(false)

    val rootFolderEmpty: Subject<Boolean> = PublishSubject.create()

    val openFolder: Subject<String> = PublishSubject.create()

    init {
        val url = settings.getServerURL()
        filesRepository.updateRepoURL(url)
    }

    fun getDialogData(): Single<String> {
        return settings.getServerUrl()
    }

    fun updateServerUrl(url: String) {
        filesRepository.updateRepoURL(url)
        settings.saveServerURL(url)
    }

    fun getFolders(): Flowable<PagingData<Folder>>{
//        loadingIndicator.onNext(true)
        val serverUrl = settings.getServerURL()
        if(serverUrl.isEmpty()) {
            urlAvailable.onNext(false)
        } else {
            urlAvailable.onNext(true)
        }

        return filesRepository.getPagedFolders(null)
    }

    fun openFolder(folder: String){
        openFolder.onNext(folder)
    }
}