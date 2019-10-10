package com.guillermonegrete.gallery.data.source

interface SettingsRepository {

    fun getServerURL(): String

    fun saveServerURL(url: String)
}