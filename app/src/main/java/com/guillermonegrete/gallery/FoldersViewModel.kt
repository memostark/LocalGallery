package com.guillermonegrete.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

class FoldersViewModel(
    private val settings: SettingsRepository,
    private val filesRepository: FilesRepository
): ViewModel() {

    // LiveData observables
    private val _hasUrl = MutableLiveData<Boolean>()
    val hasUrl: LiveData<Boolean> = _hasUrl

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _folders = MutableLiveData<List<String>>()
    val folders: LiveData<List<String>> = _folders

    private val _openDialog = MutableLiveData<String>()
    val openDialog: LiveData<String> = _openDialog

    // RxJava observables
    val loadingIndicator: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val urlAvailable: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    init {
        val url = settings.getServerURL()
        filesRepository.updateRepoURL(url)
    }

    fun loadDialogData(){
        val serverUrl = settings.getServerURL()
        _openDialog.value = serverUrl
    }

    fun updateUrl(url: String){
        filesRepository.updateRepoURL(url)
        settings.saveServerURL(url)
        loadFolders()
    }

    fun loadFolders(){
        _dataLoading.value = true

        val serverUrl = settings.getServerURL()
        // This if may not be necessary
        if(serverUrl.isEmpty()){
            _hasUrl.value = false
        }else{
            val items = filesRepository.getFolders()
            _hasUrl.value = true
            _folders.value = items
        }
        _dataLoading.value = false
    }

    fun getFolders(): Single<List<String>>{
        loadingIndicator.onNext(true)
        val serverUrl = settings.getServerURL()

        return filesRepository.getObservableFolders()
            .compose {
                // Check if has url to show appropriate layout and folders list
                if(serverUrl.isEmpty()) {
                    urlAvailable.onNext(false)
                    Single.just(emptyList())
                } else {
                    urlAvailable.onNext(true)
                    it
                }
            }
            .doOnSuccess { loadingIndicator.onNext(false) }
            .doOnError{ loadingIndicator.onNext(false) }
    }
}