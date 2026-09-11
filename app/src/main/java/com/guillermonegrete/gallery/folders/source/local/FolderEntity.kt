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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(refs: List<FolderScopeCrossRef>)

    // Paging 3 monitors these queries automatically to stream data updates to your UI.
    @Query("SELECT * FROM folder f INNER JOIN folder_scope_cross_ref r ON f.id = r.folderId WHERE r.scopeId = :scopeId ORDER BY name ASC")
    fun getFoldersSortedByNameAsc(scopeId: String): PagingSource<Int, FolderEntity>

    @Query("SELECT * FROM folder f INNER JOIN folder_scope_cross_ref r ON f.id = r.folderId WHERE r.scopeId = :scopeId ORDER BY name DESC")
    fun getFoldersSortedByNameDesc(scopeId: String): PagingSource<Int, FolderEntity>

    @Query("SELECT * FROM folder f INNER JOIN folder_scope_cross_ref r ON f.id = r.folderId WHERE r.scopeId = :scopeId ORDER BY count ASC")
    fun getFoldersSortedByCountAsc(scopeId: String): PagingSource<Int, FolderEntity>

    @Query("SELECT * FROM folder f INNER JOIN folder_scope_cross_ref r ON f.id = r.folderId WHERE r.scopeId = :scopeId ORDER BY count DESC")
    fun getFoldersSortedByCountDesc(scopeId: String): PagingSource<Int, FolderEntity>

    @Query("DELETE FROM folder_scope_cross_ref WHERE scopeId = :scopeId")
    suspend fun clearCrossRefsByScope(scopeId: String)

    @Query("""
        DELETE FROM folder_scope_cross_ref 
        WHERE scopeId IN (
            SELECT id FROM folder_remote_keys WHERE etag = :etag
        )
    """)
    suspend fun clearCrossRefsByEtag(etag: String)

    @Query("""
        DELETE FROM folder 
        WHERE NOT EXISTS (
            SELECT 1 
            FROM folder_scope_cross_ref 
            WHERE folder_scope_cross_ref.folderId = folder.id
        )
    """)
    suspend fun cleanupOrphanedFolders()
}
