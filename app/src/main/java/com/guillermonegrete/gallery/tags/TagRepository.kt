package com.guillermonegrete.gallery.tags

import com.guillermonegrete.gallery.data.Tag
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface TagRepository {

    fun getTags(): Single<Set<Tag>>

    fun addTag(fileId: Long, tag: Tag): Single<Tag>

    fun deleteTagFromFile(fileId: Long, id: Long): Completable
}
