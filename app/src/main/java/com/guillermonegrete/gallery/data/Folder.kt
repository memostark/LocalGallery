package com.guillermonegrete.gallery.data

import android.os.Parcelable
import com.guillermonegrete.gallery.folders.models.FolderUI
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Folder(
    val name:String,
    val coverUrl: String,
    val count: Int,
    val id: Long = 0L,
): Parcelable {
    constructor(folder: FolderUI.Model) : this(folder.name, folder.coverUrl, folder.count, folder.id){
        title = folder.title
    }

    @IgnoredOnParcel
    var title: String? = null
}
