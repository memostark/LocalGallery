package com.guillermonegrete.gallery.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [SettingsModule::class]
)
object TestSettingsModule {

    @Singleton
    @Provides
    fun providesSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(TEST_PREFERENCES_FILENAME, Context.MODE_PRIVATE)
    }
}

const val TEST_PREFERENCES_FILENAME = "test_preferences"