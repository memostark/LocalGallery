package com.guillermonegrete.gallery.data.source

class FakeSettingsRepository: SettingsRepository {

    var serverUrl = ""

    override fun getServerURL(): String{
        return serverUrl
    }

    override fun saveServerURL(url: String) {
        serverUrl = url
    }
}