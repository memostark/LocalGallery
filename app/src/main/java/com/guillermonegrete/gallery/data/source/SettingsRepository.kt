package com.guillermonegrete.gallery.data.source

import com.guillermonegrete.gallery.common.SortDialogChecked
import io.reactivex.rxjava3.core.Single
import java.lang.reflect.Field

interface SettingsRepository {

    fun getServerURL(): String

    fun getServerUrl(): Single<String>

    fun saveServerURL(url: String)

    fun getNightMode(): Int

    fun setNightMode(mode: Int)

    fun getAutoPlayMode(): Boolean

    fun setAutoPlayVideo(enabled: Boolean)

    // Sort preferences
    fun getFolderSort(): SortDialogChecked

    fun setFolderSort(field: String, sort: String)

    fun getFileSort(): SortDialogChecked

    fun setFileSort(field: String, sort: String)
}
