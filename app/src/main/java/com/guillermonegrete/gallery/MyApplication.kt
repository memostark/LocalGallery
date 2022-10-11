package com.guillermonegrete.gallery

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.guillermonegrete.gallery.data.source.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

@HiltAndroidApp
class MyApplication: Application() {

    @Inject lateinit var preferences: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(DebugTree())

        val themePref = preferences.getNightMode()
        AppCompatDelegate.setDefaultNightMode(themePref)
    }
}
