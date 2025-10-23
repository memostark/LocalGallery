package com.guillermonegrete.gallery.files.details

import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.data.TagType
import com.guillermonegrete.gallery.tags.TagRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


class AddTagViewModelTest {

    private lateinit var viewModel: AddTagViewModel
    @MockK(relaxed = true)
    private lateinit var tagRepository: TagRepository

    val fileTags = setOf(Tag("tag_file"), Tag("tag_file_2"))
    val folderTags = setOf(Tag("tag_folder"), Tag("tag_folder"))

    @Before
    fun setUp(){
        MockKAnnotations.init(this)
    }

    @Test
    fun `Given file type dialog, when initialize, then load file tags`() {
        viewModel = AddTagViewModel(TagType.File, listOf(1L), fileTags,  tagRepository)

        verify { tagRepository.getFileTags() }
        verify(exactly = 0) { tagRepository.getFolderTags(any()) }
        assertEquals(fileTags, viewModel.appliedTags)
    }

    @Test
    fun `Given single folder type dialog, when initialize, then load all folder tags and the single folder tags`() {
        val singleId = 2L
        every { tagRepository.getFolderTags(singleId) } returns Single.just(folderTags)
        viewModel = AddTagViewModel(TagType.Folder, listOf(singleId), setOf(),  tagRepository)

        verify { tagRepository.getFolderTags() }
        assertEquals(folderTags, viewModel.appliedTags)
    }

    @Test
    fun `Given multiple folder type dialog, when initialize, then load all folder tags`() {
        val initialTags = setOf<Tag>()
        viewModel = AddTagViewModel(TagType.Folder, listOf(3L, 4L), initialTags,  tagRepository)

        verify { tagRepository.getFolderTags() }
        assertEquals(initialTags, viewModel.appliedTags)
    }
}
