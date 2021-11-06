package com.guillermonegrete.gallery

import android.app.Application
import com.guillermonegrete.gallery.di.ApplicationGraph
import com.guillermonegrete.gallery.di.DaggerApplicationGraph
import timber.log.Timber
import timber.log.Timber.DebugTree


class MyApplication: Application() {
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
    }
}