package com.guillermonegrete.gallery

import android.app.Application
import dagger.hilt.android.testing.CustomTestApplication
import timber.log.Timber

@CustomTestApplication(TestSpeakableApplication::class)
interface HiltTestApplication

open class TestSpeakableApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
