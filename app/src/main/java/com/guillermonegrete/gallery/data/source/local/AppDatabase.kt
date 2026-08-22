package com.guillermonegrete.gallery.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.guillermonegrete.gallery.folders.source.local.FolderDao
import com.guillermonegrete.gallery.folders.source.local.FolderEntity
import com.guillermonegrete.gallery.folders.source.local.FolderRemoteKey
import com.guillermonegrete.gallery.folders.source.local.FolderRemoteKeyDao

@Database(entities = [FolderEntity::class, FolderRemoteKey::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun folderRemoteKeyDao(): FolderRemoteKeyDao
    abstract fun folderDao(): FolderDao
}
