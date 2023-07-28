package com.guillermonegrete.gallery.folders

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Test

import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.source.SettingsRepository
import com.guillermonegrete.gallery.di.SettingsModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@UninstallModules(SettingsModule::class)
@HiltAndroidTest
class MainActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun when_no_url_given_new_url_then_list_loaded() {

        val activityScenario = ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.message_icon)).perform(ViewActions.click())

        activityScenario.close()
    }

}