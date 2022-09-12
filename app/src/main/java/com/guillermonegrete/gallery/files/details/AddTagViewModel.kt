package com.guillermonegrete.gallery.files.details

import androidx.lifecycle.ViewModel
import com.guillermonegrete.gallery.tags.TagRepository
import javax.inject.Inject

class AddTagViewModel @Inject constructor (private val tagRepository: TagRepository): ViewModel() {

    fun getAllTags() = tagRepository.getTags()

}
