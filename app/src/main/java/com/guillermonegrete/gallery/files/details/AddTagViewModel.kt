package com.guillermonegrete.gallery.files.details

import androidx.lifecycle.ViewModel
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.tags.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

@HiltViewModel
class AddTagViewModel @Inject constructor (private val tagRepository: TagRepository): ViewModel() {

    val appliedTags = mutableSetOf<Tag>()

    fun getFileTags() = tagRepository.getFileTags()

    fun getFolderTags() = tagRepository.getFolderTags()

    fun addTag(id: Long, tag: Tag): Single<Tag> =
        tagRepository.addTag(id, tag)
        .doOnSuccess { appliedTags.add(it) }

    fun addFolderTag(id: Long, tag: Tag): Single<Tag> =
        tagRepository.addFolderTag(id, tag)
            .doOnSuccess { appliedTags.add(it) }

    fun deleteTagFromFile(fileId: Long, tag: Tag): Completable =
        tagRepository.deleteTagFromFile(fileId, tag.id)
            .doOnComplete { appliedTags.remove(tag) }

    fun deleteTagFromFolder(folderId: Long, tag: Tag): Completable =
        tagRepository.deleteTagFromFolder(folderId, tag.id)
            .doOnComplete { appliedTags.remove(tag) }

    fun addTagToFiles(id: Long, fileIds: List<Long>) = tagRepository.addTagToFiles(id, fileIds)

    fun getFolderTags(folderId: Long) = tagRepository.getFolderTags(folderId)
}
