package com.guillermonegrete.gallery.data.source

import android.content.Context
import android.preference.PreferenceManager

class DefaultSettingsRepository(context: Context): SettingsRepository {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun getServerURL(): String{
        return preferences.getString(SERVER_URL_KEY, "") ?: ""
    }

    override fun saveServerURL(url: String) {
        val editor = preferences.edit()
        editor.putString(SERVER_URL_KEY, url)
        editor.apply()
    }

    companion object{
        const val SERVER_URL_KEY = "server_url"
    }
}