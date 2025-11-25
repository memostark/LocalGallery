package com.guillermonegrete.gallery.folders

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
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
    fun given_no_url_when_new_url_then_list_loaded() {
        server.enqueue(MockResponse().setBody(FOLDER_RESPONSE))

        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.message_icon)).check(matches(isDisplayed()))

        // Add new  url
        clickSetServerMenuItem()
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
        clickSetServerMenuItem()
        onView(withId(R.id.server_address_edit)).perform(replaceText("")) // Use replaceText because typeText doesn't work a 2nd time
        clickOkDialog()

        // No list, error icon is displayed
        onView(withId(R.id.message_icon)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun when_navigate_to_files_and_back_then_list_not_updated() {
        setMockServerUrl()

        server.enqueue(MockResponse().setBody(FOLDER_RESPONSE))
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)

        // Navigate to files list of first folder
        server.enqueue(MockResponse().setBody(FILES_INFO_RESPONSE))
        server.enqueue(MockResponse().setBody(FILES_RESPONSE))
        onView(withId(R.id.folders_list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ViewHolder>(1, click()) // index 0 is header
            )

        onView(withId(R.id.files_list))
            .check(matches(atPosition(0, isDisplayed())))

        // Navigate back
        server.enqueue(MockResponse().setBody(FOUR_FOLDER_RESPONSE))
        Espresso.pressBack()

        // Verify list didn't change
        onView(withId(R.id.folders_list))
            .check(matches(atPosition(0, withText("root_folder")))) // Header
        onView(withId(R.id.folders_list))
            .check(matches(atPosition(1, hasDescendant(withText("test item"))))) // First item

        activityScenario.close()
    }

    private fun clickSetServerMenuItem() {
        val context = getInstrumentation().targetContext
        openActionBarOverflowOrOptionsMenu(context)

        val setServerText = context.getString(R.string.set_server_menu_title)
        onView(withText(setServerText)).perform(click())
    }

    private fun clickOkDialog() {
        onView(withText("OK"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())
    }

    private fun setMockServerUrl(){
        val fullUrl = server.url("/")
        settingsRepository.saveServerURL(fullUrl.host + ":" + fullUrl.port)
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

        val FOUR_FOLDER_RESPONSE = """
            {
              "name": "root_folder",
              "page": {
                "items": [
                  ${(1..4).joinToString(",") {"""
                    {
                    "name": "folder $it",
                    "coverUrl": "doesn't matter",
                    "count": 10,
                    "id": 0
                    }"""}}
                ],
                "totalPages": 1,
                "totalItems": 4
              }
            }
        """.trimIndent()

        val FILES_INFO_RESPONSE = """
            {
                "thumbnail_sizes": {
                    "original": 0,
                    "small": 100,
                    "medium": 350,
                    "large": 600,
                    "extralarge": 850
                }
            }
        """.trimIndent()

        val FILES_RESPONSE = """
            {
              "items": [
                  {
                    "url": "dummy-url",
                    "filename": "file_1.jpg",
                    "width": 200,
                    "height": 300,
                    "creationDate": "2020-08-25T08:49:31Z",
                    "lastModified": "2021-10-27T19:56:10Z",
                    "tags": [],
                    "id": 0,
                    "file_type":"Image"
                  }
                ],
                "totalPages": 1,
                "totalItems": 1
            }
        """.trimIndent()
    }

}