package com.guillermonegrete.gallery.data

import android.os.Parcelable
import com.guillermonegrete.gallery.folders.models.FolderUI
import com.guillermonegrete.gallery.folders.source.local.FolderEntity
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Folder(
    val name:String,
    val coverUrl: String,
    val count: Int,
    val id: Long = 0L,
): Parcelable {
    fun toEntity() = FolderEntity(name, coverUrl, count, id.toInt())

    constructor(folder: FolderUI.Model) : this(folder.name, folder.coverUrl, folder.count, folder.id){
        title = folder.title
    }

    @IgnoredOnParcel
    var title: String? = null

    companion object {
        val NULL_FOLDER = Folder("", "", 0)
    }
}
