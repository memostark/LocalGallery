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
    @PrimaryKey val id: Int,
) {
    fun toDomainModel() = Folder(name, coverUrl, count, id.toLong())
}

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(folders: List<FolderEntity>)

    // Paging 3 monitors this query automatically to stream data updates to your UI.
    // Order it by whatever attribute makes sense for your listing (e.g., name, ID, or index)
    @Query("SELECT * FROM folder ORDER BY name ASC")
    fun getFoldersPagingSource(): PagingSource<Int, FolderEntity>

    @Query("DELETE FROM folder")
    suspend fun clearAllFolders()
}
