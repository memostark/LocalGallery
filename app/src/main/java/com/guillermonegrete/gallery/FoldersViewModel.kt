package com.guillermonegrete.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FoldersViewModel(private val settings: SettingsRepository): ViewModel() {

    private val _hasUrl = MutableLiveData<Boolean>()
    val hasUrl: LiveData<Boolean> = _hasUrl

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    fun loadFolders(){
        _dataLoading.value = true

        val serverUrl = settings.getServerURL()
        if(serverUrl.isEmpty()){
            _dataLoading.value = false
            _hasUrl.value = false
        }else{

        }
    }
}