package com.guillermonegrete.gallery.tags

import javax.inject.Inject

class DefaultTagRepository @Inject constructor (private val tagService: TagService): TagRepository {

    override fun getTags() = tagService.getAllTags()
}
