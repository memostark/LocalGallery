package com.guillermonegrete.gallery.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.guillermonegrete.gallery.files.source.local.FileDao
import com.guillermonegrete.gallery.files.source.local.FileEntity
import com.guillermonegrete.gallery.files.source.local.FileRemoteKey
import com.guillermonegrete.gallery.files.source.local.FileRemoteKeyDao
import com.guillermonegrete.gallery.files.source.local.FileScopeCrossRef
import com.guillermonegrete.gallery.files.source.local.FileTagCrossRef
import com.guillermonegrete.gallery.folders.source.local.FolderDao
import com.guillermonegrete.gallery.folders.source.local.FolderEntity
import com.guillermonegrete.gallery.folders.source.local.FolderRemoteKey
import com.guillermonegrete.gallery.folders.source.local.FolderRemoteKeyDao
import com.guillermonegrete.gallery.folders.source.local.FolderScopeCrossRef
import com.guillermonegrete.gallery.tags.source.local.TagDao
import com.guillermonegrete.gallery.tags.source.local.TagEntity

@Database(
    entities = [
        FolderEntity::class,
        FolderRemoteKey::class,
        FolderScopeCrossRef::class,
        FileEntity::class,
        FileRemoteKey::class,
        FileScopeCrossRef::class,
        TagEntity::class,
        FileTagCrossRef::class,
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun folderRemoteKeyDao(): FolderRemoteKeyDao
    abstract fun folderDao(): FolderDao

    abstract fun fileRemoteKeyDao(): FileRemoteKeyDao
    abstract fun fileDao(): FileDao

    abstract fun tagDao(): TagDao
}
