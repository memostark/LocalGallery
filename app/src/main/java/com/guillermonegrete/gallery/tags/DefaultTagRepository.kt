package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.data.Tag
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class DefaultTagRepository @Inject constructor (private val tagService: TagService): TagRepository {

    override fun getTags() = tagService.getAllTags()

    override fun getTags(folderId: Long) = tagService.getFolderTags(folderId)

    override fun addTag(fileId: Long, tag: Tag): Single<Tag> = tagService.addTag(fileId, tag)

    override fun addTagToFiles(tagId: Long, fileIds: List<Long>) = tagService.addTagToFiles(tagId, fileIds)

    override fun deleteTagFromFile(fileId: Long, id: Long) = tagService.deleteTagFromFile(fileId, id)
}
