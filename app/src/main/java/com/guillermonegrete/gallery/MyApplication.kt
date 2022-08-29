package com.guillermonegrete.gallery

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.guillermonegrete.gallery.data.source.SettingsRepository
import com.guillermonegrete.gallery.di.ApplicationGraph
import com.guillermonegrete.gallery.di.DaggerApplicationGraph
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject


class MyApplication: Application() {

    @Inject lateinit var preferences: SettingsRepository

    val appComponent: ApplicationGraph by lazy {
        initializeComponent()
    }

    private fun initializeComponent(): ApplicationGraph {
        // Creates an instance of AppComponent using its Factory constructor
        return DaggerApplicationGraph.factory().create(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(DebugTree())
        appComponent.inject(this)

        val themePref = preferences.getNightMode()
        AppCompatDelegate.setDefaultNightMode(themePref)
    }
}
