package com.guillermonegrete.gallery.files.source.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "file_remote_keys")
data class FileRemoteKey(
    @PrimaryKey val id: String,
    val nextKey: Int?,
    val etag: String?,
)

object FileScopeHelper {
    fun generateScopeId(
        folderId: Long?,
        sort: String,
        folderTags: List<Long>?,
        fileTags: List<Long>?,
    ) = if (folderId != null) {
        val tags = (folderTags.orEmpty() + fileTags.orEmpty()).sorted()
        val tagString = if (tags.isEmpty()) "" else tags.joinToString(",", "_tags_[", "]")
        "files_folder_${folderId}${tagString}_sort_${sort}"
    } else {
        val folderTagsString = if (folderTags.isNullOrEmpty()) "" else folderTags.sorted().joinToString(",", "_folderTags_[", "]")
        val fileTagsString = if (fileTags.isNullOrEmpty()) "" else fileTags.sorted().joinToString(",", "_fileTags_[", "]")
        "files_overall${folderTagsString}${fileTagsString}_sort_${sort}"
    }
}

@Dao
interface FileRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: FileRemoteKey)

    @Query("SELECT * FROM file_remote_keys WHERE id = :id LIMIT 1")
    suspend fun getRemoteKeyById(id: String): FileRemoteKey?

    @Query("DELETE FROM file_remote_keys WHERE etag = :etag")
    suspend fun deleteKeysByEtag(etag: String)
}


@Entity(tableName = "file_scope_cross_ref", primaryKeys = ["scopeId", "fileId"])
data class FileScopeCrossRef(
    val scopeId: String,
    val fileId: Long,
)
