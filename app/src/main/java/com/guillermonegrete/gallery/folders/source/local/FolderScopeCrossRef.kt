package com.guillermonegrete.gallery.folders.source.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "folder_scope_cross_ref",
    primaryKeys = ["scopeId", "folderId"],
    indices = [Index(value = ["folderId"])]
)
data class FolderScopeCrossRef(
    val scopeId: String,
    val folderId: Int
)
