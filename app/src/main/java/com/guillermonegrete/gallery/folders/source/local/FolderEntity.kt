package com.guillermonegrete.gallery.folders.source.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.guillermonegrete.gallery.data.Folder

@Entity(tableName = "folder")
data class FolderEntity(
    val name:String,
    val coverUrl: String,
    val count: Int,
    val scopeId: String,
    @PrimaryKey val id: Int,
) {
    fun toDomainModel() = Folder(name, coverUrl, count, id.toLong())
}

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(folders: List<FolderEntity>)

    // Paging 3 monitors these queries automatically to stream data updates to your UI.
    @Query("SELECT * FROM folder WHERE scopeId = :scopeId ORDER BY name ASC")
    fun getFoldersSortedByNameAsc(scopeId: String): PagingSource<Int, FolderEntity>

    @Query("SELECT * FROM folder WHERE scopeId = :scopeId ORDER BY name DESC")
    fun getFoldersSortedByNameDesc(scopeId: String): PagingSource<Int, FolderEntity>

    @Query("SELECT * FROM folder WHERE scopeId = :scopeId ORDER BY count ASC")
    fun getFoldersSortedByCountAsc(scopeId: String): PagingSource<Int, FolderEntity>

    @Query("SELECT * FROM folder WHERE scopeId = :scopeId ORDER BY count DESC")
    fun getFoldersSortedByCountDesc(scopeId: String): PagingSource<Int, FolderEntity>

    @Query("DELETE FROM folder WHERE scopeId = :scopeId")
    suspend fun clearFoldersByScope(scopeId: String)

    @Query("DELETE FROM folder WHERE scopeId LIKE 'folders_regular_%'")
    suspend fun clearAllRegularFeedFolders()
}
