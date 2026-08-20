package com.guillermonegrete.gallery.folders.source.local

import androidx.room3.Dao
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query

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
