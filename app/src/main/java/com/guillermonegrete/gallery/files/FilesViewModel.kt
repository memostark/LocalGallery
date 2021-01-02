package com.guillermonegrete.gallery.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava2.cachedIn
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class FilesViewModel @Inject constructor(
    settings: SettingsRepository,
    private val filesRepository: FilesRepository
): ViewModel() {

    val openDetails: Subject<Int> = PublishSubject.create()

    var cachedFileList: Flowable<PagingData<File>>? = null
        private set

    private var currentFolder: String? = null

    init {
        val url = settings.getServerURL()
        filesRepository.updateRepoURL(url)
    }

    fun loadPagedFiles(folder: String): Flowable<PagingData<File>>{
        val lastResult = cachedFileList
        if (currentFolder == folder && lastResult != null) return lastResult

        val newResult = filesRepository.getPagedFiles(folder).cachedIn(viewModelScope)
        currentFolder = folder
        cachedFileList = newResult
        return newResult
    }

    fun openFilesDetails(index: Int){
        openDetails.onNext(index)
    }
}