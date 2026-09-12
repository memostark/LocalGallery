package com.guillermonegrete.gallery.data.source

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CachePreferenceManager @Inject constructor(@ApplicationContext context: Context) {
    private val sharedPrefs = context.getSharedPreferences("app_cache_prefs", Context.MODE_PRIVATE)

    fun getFolderName(): String? {
        return sharedPrefs.getString(FOLDER_NAME_KEY, null)
    }

    fun saveFolderName(name: String?) {
        sharedPrefs.edit { putString(FOLDER_NAME_KEY, name) }
    }

    companion object {
        private const val FOLDER_NAME_KEY = "global_folder_name"
    }
}
