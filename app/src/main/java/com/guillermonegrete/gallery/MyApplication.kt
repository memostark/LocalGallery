package com.guillermonegrete.gallery

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.guillermonegrete.gallery.data.source.SettingsRepository
import com.guillermonegrete.gallery.di.ApplicationGraph
import dagger.hilt.EntryPoints
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

@HiltAndroidApp
class MyApplication: Application() {

    @Inject lateinit var preferences: SettingsRepository

    val appComponent: ApplicationGraph by lazy {
        initializeComponent()
    }

    private fun initializeComponent(): ApplicationGraph {
        return EntryPoints.get(this, ApplicationGraph::class.java)
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(DebugTree())

        val themePref = preferences.getNightMode()
        AppCompatDelegate.setDefaultNightMode(themePref)
    }
}
