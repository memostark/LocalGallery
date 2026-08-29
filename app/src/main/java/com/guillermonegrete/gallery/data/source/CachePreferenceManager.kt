package com.guillermonegrete.gallery.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cache_prefs")

class CachePreferenceManager @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private val ETAG_KEY = stringPreferencesKey("global_folder_etag")
    }

    suspend fun getGlobalFolderEtag(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[ETAG_KEY]
        }.first()
    }

    suspend fun saveGlobalFolderEtag(etag: String?) {
        context.dataStore.edit { preferences ->
            if (etag != null) {
                preferences[ETAG_KEY] = etag
            } else {
                preferences.remove(ETAG_KEY)
            }
        }
    }
}
