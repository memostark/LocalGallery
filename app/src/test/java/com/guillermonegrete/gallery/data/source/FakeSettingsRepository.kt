package com.guillermonegrete.gallery.data.source

import com.guillermonegrete.gallery.common.SortDialogChecked
import io.reactivex.rxjava3.core.Single

class FakeSettingsRepository: SettingsRepository {

    var serverUrl = ""

    override fun getServerURL(): String{
        return serverUrl
    }

    override fun getServerUrl(): Single<String> {
        return Single.just(serverUrl)
    }

    override fun saveServerURL(url: String) {
        serverUrl = url
    }

    override fun getNightMode(): Int {
        TODO("Not yet implemented")
    }

    override fun setNightMode(mode: Int) {
        TODO("Not yet implemented")
    }

    override fun getFolderSort(): SortDialogChecked = SortDialogChecked.DEFAULT_FOLDER

    override fun setFolderSort(field: String, sort: String) {
        TODO("Not yet implemented")
    }

    override fun getFileSort(): SortDialogChecked = SortDialogChecked.DEFAULT_FILE

    override fun setFileSort(field: String, sort: String) {
        TODO("Not yet implemented")
    }
}