package com.guillermonegrete.gallery.data.source

import io.reactivex.Single

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
}