package com.guillermonegrete.gallery.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.paging.rxjava3.cachedIn
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import com.guillermonegrete.gallery.folders.models.FolderUI
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

    fun getFolders(): Flowable<PagingData<FolderUI>>{
        val serverUrl = settings.getServerURL()
        if(serverUrl.isEmpty()) {
            urlAvailable.onNext(false)
        } else {
            urlAvailable.onNext(true)
        }

        return filesRepository.getPagedFolders(null)
            .map { pagingData ->
                pagingData.map { FolderUI.Model(it) }.insertSeparators { before: FolderUI.Model?, after: FolderUI.Model? ->
                    if(before == null) return@insertSeparators FolderUI.HeaderModel(after?.title ?: "")
                    return@insertSeparators null
                }
            }.cachedIn(viewModelScope)
    }

    fun openFolder(folder: String){
        openFolder.onNext(folder)
    }
}