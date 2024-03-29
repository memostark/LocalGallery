package com.guillermonegrete.gallery.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.paging.rxjava3.cachedIn
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import com.guillermonegrete.gallery.folders.models.FolderUI
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val filesRepository: FilesRepository
): ViewModel() {

    private val forceUpdate: Subject<Boolean> = BehaviorSubject.createDefault(true)

    val urlAvailable: Subject<Boolean> = PublishSubject.create()

    private var searchQuery: Subject<String> = BehaviorSubject.createDefault("")

    private val sort: Subject<String> = PublishSubject.create()

    var folderSelection = -1

    val pagedFolders = sort.distinctUntilChanged().switchMap { filter ->
        searchQuery.distinctUntilChanged().switchMap { query ->
            val finalQuery = query.ifEmpty { null }
            forceUpdate.switchMap {
                filesRepository.getPagedFolders(finalQuery, filter)
                    .map { pagingData ->
                        pagingData.map { folder -> FolderUI.Model(folder) }
                            .insertSeparators { before: FolderUI.Model?, after: FolderUI.Model? ->
                                if (before == null && after != null)
                                    return@insertSeparators FolderUI.HeaderModel(after.title ?: "")
                                return@insertSeparators null
                            }
                    }.toObservable()
            }
        }
    }.toFlowable(BackpressureStrategy.LATEST).cachedIn(viewModelScope)

    fun getDialogData(): Single<String> {
        return settings.getServerUrl()
    }

    fun updateServerUrl(url: String) {
        settings.saveServerURL(url)
        if(url.isEmpty()) {
            urlAvailable.onNext(false)
        } else {
            urlAvailable.onNext(true)
            val sorting = settings.getFolderSort()
            sort.onNext("${sorting.field.field},${sorting.sort.oder}")
        }
    }

    fun getFolders() {
        val serverUrl = settings.getServerURL()

        if(serverUrl.isEmpty()) {
            urlAvailable.onNext(false)
        } else {
            urlAvailable.onNext(true)
            val sorting = settings.getFolderSort()
            sort.onNext("${sorting.field.field},${sorting.sort.oder}")
        }
    }

    fun updateFilter(query: CharSequence) {
        searchQuery.onNext(query.toString())
    }

    fun updateSort(field: String, order: String) {
        settings.setFolderSort(field, order)
        sort.onNext("$field,$order")
    }

    fun refresh(){
        forceUpdate.onNext(true)
    }

    fun setAutoplayVideo(checked: Boolean) {
        settings.setAutoPlayVideo(checked)
    }
}
