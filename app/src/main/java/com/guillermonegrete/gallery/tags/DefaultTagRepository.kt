package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.data.Tag
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class DefaultTagRepository @Inject constructor (private val tagService: TagService): TagRepository {

    override fun getTags() = tagService.getAllTags()

    override fun getTags(folderId: Long) = tagService.getTags(folderId)

    override fun addTag(fileId: Long, tag: Tag): Single<Tag> = tagService.addTag(fileId, tag)

    override fun addFolderTag(fileId: Long, tag: Tag) = tagService.addFolderTag(fileId, tag)

    override fun addTagToFiles(tagId: Long, fileIds: List<Long>) = tagService.addTagToFiles(tagId, fileIds)

    override fun deleteTagFromFile(fileId: Long, id: Long) = tagService.deleteTagFromFile(fileId, id)

    override fun deleteTagFromFolder(folderId: Long, id: Long) = tagService.deleteTagFromFolder(folderId, id)

    override fun getFileTags() = tagService.getFileTags()

    override fun getFolderTags() = tagService.getFolderTags()

    override fun getFolderTags(folderId: Long) = tagService.getFolderTags(folderId)
}
