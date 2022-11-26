package com.guillermonegrete.gallery.data.source

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.guillermonegrete.gallery.common.Order
import com.guillermonegrete.gallery.common.SortDialogChecked
import com.guillermonegrete.gallery.files.SortField
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class DefaultSettingsRepository @Inject constructor(@ApplicationContext context: Context): SettingsRepository {

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

    override fun getFolderSort(): SortDialogChecked {
        val fieldString = preferences.getString(FOLDER_FIELD_KEY, null)
        val sortString = preferences.getString(FOLDER_ORDER_KEY, null)
        val field = SortField.fromField(fieldString) ?: SortField.DEFAULT_FOLDER
        val sort = Order.fromOrder(sortString)

        return SortDialogChecked(field, sort)
    }

    override fun setFolderSort(field: String, sort: String) {
        preferences.edit {
            putString(FOLDER_FIELD_KEY, field)
            putString(FOLDER_ORDER_KEY, sort)
        }
    }

    override fun getFileSort(): String {
        TODO("Not yet implemented")
    }

    override fun setFileSort(string: String) {
        TODO("Not yet implemented")
    }

    companion object{
        const val SERVER_URL_KEY = "server_url"
        const val NIGHT_MODE_KEY = "night_mode"

        // Sorting keys
        const val FOLDER_FIELD_KEY = "folder_field"
        const val FOLDER_ORDER_KEY = "folder_order"
    }
}