package com.guillermonegrete.gallery.folders.source.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "folder_remote_keys")
data class FolderRemoteKey(
    val nextKey: Int?,
    @PrimaryKey val id: String,
) {
    companion object {
        fun generateId(query: String?, sort: String): String {
            val sanitizedQuery = if (query.isNullOrBlank()) "default" else "query_${query.trim()}"
            return "folders_${sanitizedQuery}_sort_${sort}"
        }
    }
}

@Dao
interface FolderRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: FolderRemoteKey)

    @Query("SELECT * FROM folder_remote_keys WHERE id = :id LIMIT 1")
    suspend fun getRemoteKeyById(id: String): FolderRemoteKey?

    @Query("DELETE FROM folder_remote_keys WHERE id = :id")
    suspend fun deleteKeyById(id: String)

    @Query("DELETE FROM folder_remote_keys")
    suspend fun clearRemoteKeys()
}
