package com.guillermonegrete.gallery.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class FilesViewModel @Inject constructor(
    settings: SettingsRepository,
    private val filesRepository: FilesRepository
): ViewModel() {

    val openDetails: Subject<Int> = PublishSubject.create()

    private val folderName: Subject<String> = PublishSubject.create()
    private val filter: Subject<String> = BehaviorSubject.createDefault("")

    var cachedFileList: Flowable<PagingData<File>> = filter.distinctUntilChanged().switchMap { filter ->
        folderName.distinctUntilChanged().switchMap { folder ->
            val finalFilter = if(filter.isEmpty()) null else filter
            filesRepository.getPagedFiles(folder, finalFilter).toObservable()
        }
    }.toFlowable(BackpressureStrategy.LATEST).cachedIn(viewModelScope)

    init {
        val url = settings.getServerURL()
        filesRepository.updateRepoURL(url)
    }

    fun openFilesDetails(index: Int){
        openDetails.onNext(index)
    }

    fun setFolderName(name: String){
        folderName.onNext(name)
    }

    fun setFilter(filterBy: String){
        filter.onNext(filterBy)
    }
}
