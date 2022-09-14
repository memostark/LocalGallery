package com.guillermonegrete.gallery.files.details

import androidx.lifecycle.ViewModel
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.tags.TagRepository
import javax.inject.Inject

class AddTagViewModel @Inject constructor (private val tagRepository: TagRepository): ViewModel() {

    fun getAllTags() = tagRepository.getTags()

    fun addTag(id: Long, tag: Tag) = tagRepository.addTag(id, tag)

    fun deleteTagFromFile(fileId: Long, id: Long) = tagRepository.deleteTagFromFile(fileId, id)
}
