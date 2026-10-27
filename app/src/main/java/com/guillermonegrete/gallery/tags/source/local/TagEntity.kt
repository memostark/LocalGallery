package com.guillermonegrete.gallery.tags.source.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import com.guillermonegrete.gallery.data.Tag
import java.util.Date

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val creationDate: Long,
    val count: Long?,
    val tagType: Int,
) {
    fun toDomainModel() = Tag(name, Date(creationDate), id)
}

fun Tag.toFileEntity(): TagEntity {
    return TagEntity(id, this.name, this.creationDate.time, this.count, TagType.FILE.value)
}

enum class TagType(val value: Int) {
    FILE(1)
}

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTags(tags: List<TagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)
}