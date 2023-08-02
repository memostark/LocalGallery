package com.guillermonegrete.gallery.data.source

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.guillermonegrete.gallery.common.Order
import com.guillermonegrete.gallery.common.SortDialogChecked
import com.guillermonegrete.gallery.files.SortField
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class DefaultSettingsRepository @Inject constructor(private val preferences: SharedPreferences): SettingsRepository {

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

    override fun getFileSort(): SortDialogChecked {
        val fieldString = preferences.getString(FILE_FIELD_KEY, null)
        val sortString = preferences.getString(FILE_ORDER_KEY, null)
        val field = SortField.fromField(fieldString) ?: SortField.DEFAULT
        val sort = Order.fromOrder(sortString)

        return SortDialogChecked(field, sort)
    }

    override fun setFileSort(field: String, sort: String) {
        preferences.edit {
            putString(FILE_FIELD_KEY, field)
            putString(FILE_ORDER_KEY, sort)
        }
    }

    companion object{
        const val SERVER_URL_KEY = "server_url"
        const val NIGHT_MODE_KEY = "night_mode"

        // Sorting keys
        const val FOLDER_FIELD_KEY = "folder_field"
        const val FOLDER_ORDER_KEY = "folder_order"
        const val FILE_FIELD_KEY = "file_field"
        const val FILE_ORDER_KEY = "file_order"
    }
}