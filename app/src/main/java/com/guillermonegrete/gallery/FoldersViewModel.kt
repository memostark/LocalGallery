package com.guillermonegrete.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.guillermonegrete.gallery.data.source.FilesRepository

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

    fun loadFolders(){
        _dataLoading.value = true

        val serverUrl = settings.getServerURL()
        if(serverUrl.isEmpty()){
            _dataLoading.value = false
            _hasUrl.value = false
        }else{
            val items = filesRepository.getFolders()
            _folders.value = items
        }
    }
}