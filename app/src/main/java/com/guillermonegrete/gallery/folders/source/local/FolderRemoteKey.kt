package com.guillermonegrete.gallery.folders.source.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "folder_remote_keys")
data class FolderRemoteKey(
    val eTag: String?,               // The page-specific ETag returned by the backend
    val nextKey: Int?,               // The next page number to fetch
    @PrimaryKey val id: String = "global_folder_key",
)

@Dao
interface FolderRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: FolderRemoteKey)

    @Query("SELECT * FROM folder_remote_keys WHERE id = 'global_folder_key' LIMIT 1")
    suspend fun getGlobalRemoteKey(): FolderRemoteKey?

    @Query("DELETE FROM folder_remote_keys")
    suspend fun clearRemoteKeys()
}
