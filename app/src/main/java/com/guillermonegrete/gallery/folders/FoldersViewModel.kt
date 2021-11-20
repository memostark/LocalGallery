package com.guillermonegrete.gallery.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.paging.rxjava3.cachedIn
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import com.guillermonegrete.gallery.folders.models.FolderUI
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import javax.inject.Inject

class FoldersViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val filesRepository: FilesRepository
): ViewModel() {

    val urlAvailable: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val openFolder: Subject<String> = PublishSubject.create()

    private val urlFolder: Subject<String> = PublishSubject.create()

    private val searchQuery: Subject<String> = BehaviorSubject.createDefault("")

    val pagedFolders = urlFolder.switchMap {
        searchQuery.switchMap { query ->
            val finalQuery = if(query.isEmpty()) null else query
            filesRepository.getPagedFolders(finalQuery, null)
                .map { pagingData ->
                    pagingData.map { folder -> FolderUI.Model(folder) }.insertSeparators { before: FolderUI.Model?, after: FolderUI.Model? ->
                        if(before == null && after != null) return@insertSeparators FolderUI.HeaderModel(after.title ?: "")
                        return@insertSeparators null
                    }
                }.toObservable()
        }
    }.toFlowable(BackpressureStrategy.LATEST).cachedIn(viewModelScope)

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

    fun getFolders(){
        val serverUrl = settings.getServerURL()
        if(serverUrl.isEmpty()) {
            urlAvailable.onNext(false)
        } else {
            urlAvailable.onNext(true)
            urlFolder.onNext(serverUrl)
        }
    }

    fun openFolder(folder: String){
        openFolder.onNext(folder)
    }

    fun updateFilter(query: CharSequence) {
        searchQuery.onNext(query.toString())
    }
}
