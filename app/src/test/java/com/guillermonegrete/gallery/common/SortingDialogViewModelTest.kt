package com.guillermonegrete.gallery.common

import com.guillermonegrete.gallery.tags.TagRepository
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SortingDialogViewModelTest {

    private lateinit var viewModel: SortingDialogViewModel
    @MockK(relaxed = true)
    private lateinit var tagRepository: TagRepository

    @Before
    fun setUp(){
        MockKAnnotations.init(this)
    }

    @Test
    fun `Given folder tag arg, when get tags, then only folder tags`() {
        viewModel = SortingDialogViewModel(SortingDialog.FOLDER_TAGS, tagRepository)

        verify { tagRepository.getFolderTags() }
    }


    @Test
    fun `Given all tags arg, when get tags, then fetch all tags`() {
        viewModel = SortingDialogViewModel(SortingDialog.GET_ALL_TAGS, tagRepository)

        verify { tagRepository.getTags() }
    }

    @Test
    fun `Given folder id arg, when get tags, then get tags for the folder`() {
        val folderId = 10L
        viewModel = SortingDialogViewModel(folderId, tagRepository)

        verify { tagRepository.getTags(folderId) }
    }
}
