package com.guillermonegrete.gallery.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.paging.rxjava3.cachedIn
import com.guillermonegrete.gallery.common.Order
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import com.guillermonegrete.gallery.files.SortField
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

    private val defaultFilter: String = "${DEFAULT_FIELD.field},${Order.DEFAULT.oder}"

    private val forceUpdate: Subject<Boolean> = BehaviorSubject.createDefault(true)

    val urlAvailable: Subject<Boolean> = PublishSubject.create()

    private val urlFolder: Subject<String> = PublishSubject.create()

    private val searchQuery: Subject<String> = BehaviorSubject.createDefault(defaultFilter)

    private val sort: Subject<String> = BehaviorSubject.createDefault("")

    val pagedFolders = sort.distinctUntilChanged().switchMap { filter ->
        urlFolder.distinctUntilChanged().switchMap {
            searchQuery.distinctUntilChanged().switchMap { query ->
                val finalQuery = query.ifEmpty { null }
                val finalFilter = filter.ifEmpty { defaultFilter }
                forceUpdate.switchMap {
                    filesRepository.getPagedFolders(finalQuery, finalFilter)
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
        }
    }.toFlowable(BackpressureStrategy.LATEST).cachedIn(viewModelScope)

    fun getDialogData(): Single<String> {
        return settings.getServerUrl()
    }

    fun updateServerUrl(url: String) {
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

    fun updateFilter(query: CharSequence) {
        searchQuery.onNext(query.toString())
    }

    fun updateSort(query: CharSequence) {
        sort.onNext(query.toString())
    }

    fun refresh(){
        forceUpdate.onNext(true)
    }

    companion object{
        val DEFAULT_FIELD = SortField.NAME
    }
}
