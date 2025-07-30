package com.guillermonegrete.gallery.common

import androidx.lifecycle.ViewModel
import com.guillermonegrete.gallery.common.SortingDialog.Companion.GET_ALL_TAGS
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.tags.TagRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import timber.log.Timber

@HiltViewModel(assistedFactory = SortingDialogViewModel.Factory::class)
class SortingDialogViewModel @AssistedInject constructor(
    @Assisted private val folderId: Long,
    val tagRepository: TagRepository
): ViewModel() {

    val tags: BehaviorSubject<Set<Tag>> = BehaviorSubject.createDefault(emptySet())

    private val disposable = CompositeDisposable()

    init {
        loadTags(folderId)
    }

    fun loadTags(folderId: Long) {
        val tagSource = if(folderId == GET_ALL_TAGS) tagRepository.getTags() else tagRepository.getTags(folderId)
        disposable.add(tagSource
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { tags ->
                    this.tags.onNext(tags)
                },
                Timber::e
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    @AssistedFactory
    interface Factory {
        fun create(folderId: Long): SortingDialogViewModel
    }
}
