package com.guillermonegrete.gallery.files.source.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.FileType
import com.guillermonegrete.gallery.data.ImageFile
import com.guillermonegrete.gallery.data.VideoFile
import com.guillermonegrete.gallery.folders.source.local.FolderEntity
import java.util.Date

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey val id: Long,
    val url: String,
    val filename: String,
    val width: Int,
    val height: Int,
    val creationDate: Long,
    val lastModified: Long,
    val folderId: Long?,
    val fileType: String,
    val duration: Int?
)

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<FileEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(refs: List<FileScopeCrossRef>)

    // Paging 3 monitors these queries automatically to stream data updates to your UI.
    @Query("SELECT * FROM files f INNER JOIN file_scope_cross_ref r ON f.id = r.fileId WHERE r.scopeId = :scopeId ORDER BY filename ASC")
    fun getFilesByNameAsc(scopeId: String): PagingSource<Int, FileWithFolder>

    @Query("SELECT * FROM files f INNER JOIN file_scope_cross_ref r ON f.id = r.fileId WHERE r.scopeId = :scopeId ORDER BY filename DESC")
    fun getFilesByNameDesc(scopeId: String): PagingSource<Int, FileWithFolder>

    @Query("SELECT * FROM files f INNER JOIN file_scope_cross_ref r ON f.id = r.fileId WHERE r.scopeId = :scopeId ORDER BY creationDate ASC")
    fun getFilesByCreatedAsc(scopeId: String): PagingSource<Int, FileWithFolder>

    @Query("SELECT * FROM files f INNER JOIN file_scope_cross_ref r ON f.id = r.fileId WHERE r.scopeId = :scopeId ORDER BY creationDate DESC")
    fun getFilesByCreatedDesc(scopeId: String): PagingSource<Int, FileWithFolder>

    @Query("SELECT * FROM files f INNER JOIN file_scope_cross_ref r ON f.id = r.fileId WHERE r.scopeId = :scopeId ORDER BY lastModified ASC")
    fun getFilesByModifiedAsc(scopeId: String): PagingSource<Int, FileWithFolder>

    @Query("SELECT * FROM files f INNER JOIN file_scope_cross_ref r ON f.id = r.fileId WHERE r.scopeId = :scopeId ORDER BY lastModified DESC")
    fun getFilesByModifiedDesc(scopeId: String): PagingSource<Int, FileWithFolder>

    @Query(
        """
            DELETE FROM file_scope_cross_ref 
            WHERE scopeId IN (
                SELECT id FROM file_remote_keys WHERE etag = :etag
            )
        """
    )
    suspend fun clearCrossRefsByEtag(etag: String)
}

data class FileWithFolder(
    @Embedded val file: FileEntity,

    @Relation(
        parentColumn = "folderId",
        entityColumn = "id"
    )
    val folder: FolderEntity? // Will be null for folder-specific feeds, populated for overall feeds
) {
    fun toDomainModel(): File = with(file) {
        when (FileType.valueOf(file.fileType)) {
            FileType.Image -> ImageFile(url, filename, width, height, 0, 0, Date(creationDate), Date(lastModified), emptyList(), folder?.toDomainModel(), id)
            FileType.Video -> VideoFile(url, filename, width, height, 0, 0, Date(creationDate), Date(lastModified), duration ?: 0, emptyList(), folder?.toDomainModel(), id)
        }
    }
}
