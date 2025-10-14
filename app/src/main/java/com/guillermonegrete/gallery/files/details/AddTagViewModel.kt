package com.guillermonegrete.gallery.files.details

import androidx.lifecycle.ViewModel
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.data.TagType
import com.guillermonegrete.gallery.tags.TagRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber

@HiltViewModel(assistedFactory = AddTagViewModel.Factory::class)
class AddTagViewModel @AssistedInject constructor(
    @Assisted private val tagType: TagType,
    @Assisted private val itemIds: List<Long>,
    @Assisted private val preAppliedTags: Set<Tag>,
    private val tagRepository: TagRepository
): ViewModel() {

    val tags = BehaviorSubject.create<Set<Tag>>()
    val appliedTags = mutableSetOf<Tag>()

    /**
     * The initial item tags
     */
    val itemTags = PublishSubject.create<Set<Tag>>()

    private val disposable = CompositeDisposable()

    init {
        loadTags()
        if (tagType == TagType.Folder && itemIds.size == 1) {
            loadAppliedFolderTags(itemIds.first())
        } else {
            onItemTagsLoaded(preAppliedTags)
        }
    }

    private fun loadTags() {
        val tagSource = if(tagType == TagType.File) tagRepository.getFileTags() else tagRepository.getFolderTags()
        disposable.add(tagSource.subscribe(this.tags::onNext, Timber::e))
    }

    private fun loadAppliedFolderTags(folderId: Long) {
        disposable.add(tagRepository.getFolderTags(folderId).subscribe(::onItemTagsLoaded, Timber::e))
    }

    fun addTag(id: Long, tag: Tag) =
        tagRepository.addTag(id, tag).doOnSuccess(::onTagAdded)

    fun addFolderTag(id: Long, tag: Tag) =
        tagRepository.addFolderTag(id, tag).doOnSuccess(::onTagAdded)

    fun deleteTagFromFile(fileId: Long, tag: Tag) =
        tagRepository.deleteTagFromFile(fileId, tag.id)
            .doOnComplete { appliedTags.remove(tag) }

    fun deleteTagFromFolder(folderId: Long, tag: Tag) =
        tagRepository.deleteTagFromFolder(folderId, tag.id)
            .doOnComplete { appliedTags.remove(tag) }

    fun addTagToFiles(id: Long, fileIds: List<Long>) = tagRepository.addTagToFiles(id, fileIds)

    fun addTagToFolders(id: Long, folderIds: List<Long>) = tagRepository.addTagToFolders(id, folderIds)

    fun onTagAdded(tag: Tag) {
        appliedTags.add(tag)
        // Update the stream of all the tags
        tags.value?.let {
            val newTags = it.toMutableSet()
            tags.onNext(newTags)
        }
    }

    private fun onItemTagsLoaded(tags: Set<Tag>) {
        appliedTags.addAll(tags)
        itemTags.onNext(tags)
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

    @AssistedFactory
    interface Factory {
        fun create(tagType: TagType, itemIds: List<Long>, appliedTags: Set<Tag>): AddTagViewModel
    }
}
