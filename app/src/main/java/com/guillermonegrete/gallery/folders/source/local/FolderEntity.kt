package com.guillermonegrete.gallery.folders.source.local

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query

@Entity(tableName = "folder")
data class FolderEntity(
    val name:String,
    val coverUrl: String,
    val count: Int,
    @PrimaryKey val id: Int,
)

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(folders: List<FolderEntity>)

    // Paging 3 monitors this query automatically to stream data updates to your UI.
    // Order it by whatever attribute makes sense for your listing (e.g., name, ID, or index)
    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getFoldersPagingSource(): PagingSource<Int, FolderEntity>

    @Query("DELETE FROM folders")
    suspend fun clearAllFolders()
}
