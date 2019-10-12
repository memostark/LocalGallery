package com.guillermonegrete.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository

class FoldersViewModel(
    private val settings: SettingsRepository,
    private val filesRepository: FilesRepository
): ViewModel() {

    private val _hasUrl = MutableLiveData<Boolean>()
    val hasUrl: LiveData<Boolean> = _hasUrl

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _folders = MutableLiveData<List<String>>()
    val folders: LiveData<List<String>> = _folders

    private val _openDialog = MutableLiveData<String>()
    val openDialog: LiveData<String> = _openDialog

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
        if(serverUrl.isEmpty()){
            _hasUrl.value = false
        }else{
            val items = filesRepository.getFolders()
            _hasUrl.value = true
            _folders.value = items
        }
        _dataLoading.value = false
    }
}