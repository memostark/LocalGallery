package com.guillermonegrete.gallery.folders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val filesRepository: FilesRepository
): ViewModel() {

    private val forceUpdate = BehaviorSubject.createDefault(true)

    val urlAvailable = PublishSubject.create<Boolean>()

    private var searchQuery = BehaviorSubject.createDefault("")

    private val sort = PublishSubject.create<ListFilter>()
    private val tags = BehaviorSubject.createDefault(emptyList<Long>())

    var folderSelection = -1

    var showBottomSheet by mutableStateOf<Long?>(null)
        private set

    var pagedFolders = createPageFlow()
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    fun createPageFlow() =
        tags.distinctUntilChanged().switchMap { tagIds ->
            sort.distinctUntilChanged().switchMap { filter ->
                searchQuery.distinctUntilChanged().switchMap { query ->
                    val finalQuery = query.ifEmpty { null }
                    forceUpdate.switchMap {
                        filesRepository.getPagedFolders(tagIds, finalQuery, "${filter.sortType},${filter.order}")
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
        if(url.isEmpty()) {
            // The only way to clear the flowable is by creating a new one.
            pagedFolders = createPageFlow()
            urlAvailable.onNext(false)
        } else {
            urlAvailable.onNext(true)
            val sorting = settings.getFolderSort()
            val filter = ListFilter(sorting.field.field, sorting.sort.oder)
            sort.onNext(filter)
        }
    }

    fun getFolders() {
        val serverUrl = settings.getServerURL()

        if(serverUrl.isEmpty()) {
            urlAvailable.onNext(false)
        } else {
            urlAvailable.onNext(true)
            val sorting = settings.getFolderSort()
            val filter = ListFilter(sorting.field.field, sorting.sort.oder)
            sort.onNext(filter)
        }
    }

    fun updateFilter(query: CharSequence) {
        searchQuery.onNext(query.toString())
    }

    fun updateSort(filter: ListFilter) {
        settings.setFolderSort(filter.sortType, filter.order)
        sort.onNext(filter)
    }

    fun setTag(tags: List<Long>){
        this.tags.onNext(tags)
    }

    fun getTags(): List<Long> {
        return tags.value ?: emptyList()
    }

    fun refresh(){
        forceUpdate.onNext(true)
    }

    fun setAutoplayVideo(checked: Boolean) {
        settings.setAutoPlayVideo(checked)
    }

    fun setFolderMenu(folderId: Long) {
        showBottomSheet = folderId
    }

    fun removeFolderMenu() {
        showBottomSheet = null
    }

    data class ListFilter(
        val sortType: String = SortField.DEFAULT_FOLDER.field,
        val order: String = Order.DESC.oder,
    )
}
