package com.guillermonegrete.gallery.data.source

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class DefaultSettingsRepository @Inject constructor(context: Context): SettingsRepository {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun getServerURL(): String{
        return preferences.getString(SERVER_URL_KEY, "") ?: ""
    }

    override fun getServerUrl(): Single<String> {
        return Single.just(preferences.getString(SERVER_URL_KEY, "") ?: "")
    }

    override fun saveServerURL(url: String) {
        val editor = preferences.edit()
        editor.putString(SERVER_URL_KEY, url)
        editor.apply()
    }

    override fun getNightMode(): Int {
        return preferences.getInt(NIGHT_MODE_KEY, AppCompatDelegate.MODE_NIGHT_YES)
    }

    override fun setNightMode(mode: Int) {
        preferences.edit {
            putInt(NIGHT_MODE_KEY, mode)
        }
    }

    companion object{
        const val SERVER_URL_KEY = "server_url"
        const val NIGHT_MODE_KEY = "night_mode"
    }
}