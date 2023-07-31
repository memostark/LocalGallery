package com.guillermonegrete.gallery.folders

import androidx.annotation.StringRes
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.source.SettingsRepository
import com.guillermonegrete.gallery.di.SettingsModule
import com.guillermonegrete.gallery.utils.atPosition
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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

    private lateinit var server: MockWebServer

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        hiltRule.inject()

        settingsRepository.saveServerURL("")
    }

    @After
    fun teardown(){
        server.shutdown()
    }

    @Test
    fun when_no_url_given_new_url_then_list_loaded() {
        server.enqueue(MockResponse().setBody(FOLDER_RESPONSE))

        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.message_icon)).check(matches(isDisplayed()))

        // Add new  url
        clickHiddenMenuItem(R.string.set_server_menu_title)
        val fullUrl = server.url("/")
        val url = "127.0.0.1:" + fullUrl.port
        onView(withId(R.id.server_address_edit)).perform(typeText(url))
        clickOkDialog()

        // Verify list is displayed
        onView(withId(R.id.folders_list))
            .check(matches(atPosition(0, withText("root_folder")))) // Header
        onView(withId(R.id.folders_list))
            .check(matches(atPosition(1, hasDescendant(withText("test item"))))) // First item

        // Delete url
        clickHiddenMenuItem(R.string.set_server_menu_title)
        onView(withId(R.id.server_address_edit)).perform(replaceText("")) // Use replaceText because typeText doesn't work a 2nd time
        clickOkDialog()

        // No list, error icon is displayed
        onView(withId(R.id.message_icon)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    private fun clickHiddenMenuItem(@StringRes stringId: Int) {
        val context = getInstrumentation().targetContext
        openActionBarOverflowOrOptionsMenu(context)

        val setServerText = context.getString(stringId)
        onView(withText(setServerText)).perform(click())
    }

    private fun clickOkDialog() {
        onView(withText("OK"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())
    }

    companion object {
        val FOLDER_RESPONSE = """
            {
              "name": "root_folder",
              "page": {
                "items": [
                  {
                    "name": "test item",
                    "coverUrl": "doesn't matter",
                    "count": 10,
                    "id": 0
                  }
                ],
                "totalPages": 1,
                "totalItems": 1
              }
            }
        """.trimIndent()
    }

}