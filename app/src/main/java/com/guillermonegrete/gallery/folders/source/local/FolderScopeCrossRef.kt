package com.guillermonegrete.gallery.folders.source.local

import androidx.room.Entity

@Entity(
    tableName = "folder_scope_cross_ref",
    primaryKeys = ["scopeId", "folderId"]
)
data class FolderScopeCrossRef(
    val scopeId: String,
    val folderId: Int
)
