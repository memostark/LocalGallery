package com.guillermonegrete.gallery.data.source

import io.reactivex.rxjava3.core.Single

interface SettingsRepository {

    fun getServerURL(): String

    fun getServerUrl(): Single<String>

    fun saveServerURL(url: String)

    fun getNightMode(): Int

    fun setNightMode(mode: Int)
}