package com.guillermonegrete.gallery.tags

import androidx.room.withTransaction
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.data.source.local.AppDatabase
import com.guillermonegrete.gallery.files.source.local.FileTagCrossRef
import com.guillermonegrete.gallery.tags.source.local.toFileEntity
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class DefaultTagRepository @Inject constructor(
    private val tagService: TagService,
    private val database: AppDatabase
): TagRepository {

    override fun getTags() = tagService.getAllTags()

    override fun getTags(folderId: Long) = tagService.getTags(folderId)

    override fun addTag(fileId: Long, tag: Tag): Single<Tag>
        = tagService.addTag(fileId, tag)
            .doOnSuccessTransaction { tag ->
                database.tagDao().insertTag(tag.toFileEntity())
                database.fileDao().insertTagRef(FileTagCrossRef(fileId, tag.id))
            }

    override fun addFolderTag(fileId: Long, tag: Tag) = tagService.addFolderTag(fileId, tag)

    override fun addTagToFiles(tagId: Long, fileIds: List<Long>)
        = tagService.addTagToFiles(tagId, fileIds)
            .doOnSuccessTransaction { files ->
                files.forEach { file ->
                    database.fileDao().insertTagRef(FileTagCrossRef(file.id, tagId))
                }
            }

    override fun addTagToFolders(tagId: Long, folderIds: List<Long>) = tagService.addTagToFolders(tagId, folderIds)

    override fun deleteTagFromFile(fileId: Long, id: Long)
        = tagService.deleteTagFromFile(fileId, id)
            .doOnComplete {
                runBlocking {
                    database.withTransaction {
                        database.fileDao().removeTagRef(fileId, id)
                    }
                }
            }

    override fun deleteTagFromFolder(folderId: Long, id: Long) = tagService.deleteTagFromFolder(folderId, id)

    override fun getFileTags() = tagService.getFileTags()

    override fun getFolderTags() = tagService.getFolderTags()

    override fun getFolderTags(folderId: Long) = tagService.getFolderTags(folderId)

    private inline fun <T : Any> Single<T>.doOnSuccessTransaction(
        crossinline action: suspend (T) -> Unit
    ): Single<T> {
        return this.doOnSuccess { result ->
            runBlocking {
                database.withTransaction {
                    action(result)
                }
            }
        }
    }
}
