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
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class FilesViewModel @Inject constructor(
    settings: SettingsRepository,
    private val filesRepository: FilesRepository
): ViewModel() {

    val openDetails: Subject<Int> = PublishSubject.create()

    private val folderName: Subject<String> = PublishSubject.create()

    var cachedFileList: Flowable<PagingData<File>> = folderName.distinctUntilChanged().switchMap {
        filesRepository.getPagedFiles(it).toObservable()
    }.toFlowable(BackpressureStrategy.LATEST).cachedIn(viewModelScope)

    init {
        val url = settings.getServerURL()
        filesRepository.updateRepoURL(url)
    }

    fun openFilesDetails(index: Int){
        openDetails.onNext(index)
    }

    fun setFolderName(name: String){
        println("Set folder name")
        folderName.onNext(name)
    }
}