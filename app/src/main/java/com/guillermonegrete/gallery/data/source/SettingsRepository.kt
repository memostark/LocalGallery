package com.guillermonegrete.gallery.data.source

import io.reactivex.Single

interface SettingsRepository {

    fun getServerURL(): String

    fun getServerUrl(): Single<String>

    fun saveServerURL(url: String)
}