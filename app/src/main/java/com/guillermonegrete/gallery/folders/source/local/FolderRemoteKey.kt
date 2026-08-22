package com.guillermonegrete.gallery.folders.source.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "folder_remote_keys")
data class FolderRemoteKey(
    @PrimaryKey val pageNumber: Int, // The specific page index (0, 1, 2...)
    val eTag: String?,               // The page-specific ETag returned by the backend
    val nextKey: Int?                // The next page number to fetch
)

@Dao
interface FolderRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: FolderRemoteKey)

    @Query("SELECT * FROM folder_remote_keys WHERE pageNumber = :page")
    suspend fun getRemoteKeyForPage(page: Int): FolderRemoteKey?

    @Query("DELETE FROM folder_remote_keys")
    suspend fun clearRemoteKeys()
}
