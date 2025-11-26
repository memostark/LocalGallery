package com.guillermonegrete.gallery.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.flatMap
import androidx.paging.map
import androidx.paging.rxjava3.cachedIn
import com.guillermonegrete.gallery.common.Order
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.FileInfoResponse
import com.guillermonegrete.gallery.data.Folder
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import com.guillermonegrete.gallery.data.source.remote.FilterTags
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FilesViewModel @Inject constructor(
    private val filesRepository: FilesRepository,
    private val settings: SettingsRepository
): ViewModel() {

    val imageInfo: BehaviorSubject<FileInfoResponse> = BehaviorSubject.create()
    val openDetails: Subject<Int> = PublishSubject.create()
    val updateRows: Subject<List<UpdatedRow>> = PublishSubject.create()

    private val folderName: Subject<Folder> = PublishSubject.create()
    private val filter: BehaviorSubject<ListFilter> = BehaviorSubject.createDefault(ListFilter())
    private val tags = BehaviorSubject.createDefault(FilterTags())
    private val forceUpdate: Subject<Boolean> = BehaviorSubject.createDefault(true)

    /**
     * Used to notify when the list width change (usually due to configuration changes).
     */
    private val widthSubject: Subject<Int> = PublishSubject.create()

    /**
     * Used to notify if the position was changed by swiping in the details view.
     */
    val newFilePos: Subject<Int> = PublishSubject.create()

    var audioOn = true
    val detailsState: BehaviorSubject<DetailsUIState> = BehaviorSubject.createDefault(DetailsUIState())

    private val disposable = CompositeDisposable()

    var folderId = -1L
        private set

    private var dataSize = 0

    private val arMin = 2.0f
    private val arMax = 3.0f

    var width = 0

    init {
        getFilesInfo()
    }

    private fun getFilesInfo() {
        disposable.add(
            filesRepository.getFilesInfo()
                .subscribe(imageInfo::onNext, imageInfo::onError)
        )
    }

    private val filesFlow = tags.distinctUntilChanged().switchMap { tagIds ->
        filter.distinctUntilChanged().switchMap { filter ->
            folderName.distinctUntilChanged().switchMap { folder ->
                forceUpdate.switchMap {
                    dataSize = 0
                    val finalFilter = "${filter.sortType},${filter.order}"
                    filesRepository.getPagedFiles(folder, tagIds, finalFilter).toObservable()
                }
            }
        }
    }.cachedIn(viewModelScope)

    // The order in combineLatest() is important here, widthSubject must be first so every time it changes all the emissions of files get reprocessed with the new width
    val cachedFileList = Observable.combineLatest(widthSubject, filesFlow) { width, pagingData ->
        this.width = width
        pagingData
    }.map { pagingData ->
        dataSize = 0
        pagingData.map { dataSize++; it }
    }.map { pagingData ->
        var arSum = 0f
        val tempList = mutableListOf<File>()
        val tempSizes = mutableListOf<Size>()
        val unfinishedRow = mutableListOf<Size>()

        var index = 0

        pagingData.flatMap { file ->
            val size =
                Size(file.width, file.height)
            tempList.add(file)
            tempSizes.add(size)
            arSum += getAspectRatio(size)
            index++

            when {
                arSum in arMin..arMax -> {
                    // Ratio in range, add row
                    normalizeHeights(tempSizes, width / arSum)
                    arSum = 0f
                    val files = updateSizes(tempList, tempSizes)
                    val unfinishedCount = unfinishedRow.size
                    updateUnfinishedRow(tempSizes, index, unfinishedCount)
                    tempList.clear()
                    tempSizes.clear()
                    unfinishedRow.clear()
                    // If there are any files from an unfinished row don't return them
                    // Those were already returned in index == dataSize condition
                    if(unfinishedCount < 1) files else files.subList(unfinishedCount, files.size)
                }
                arSum > arMax -> {
                    // Ratio too big, remove last and add the rest as a row
                    val pop = tempSizes.removeAt(tempSizes.lastIndex)
                    val popFile = tempList.removeAt(tempList.lastIndex)
                    val popRatio = getAspectRatio(pop)
                    arSum -= popRatio
                    normalizeHeights(tempSizes, width / arSum)
                    val files = updateSizes(tempList, tempSizes)
                    val unfinishedCount = unfinishedRow.size
                    // index minus one because the current item is not part of this row but the next
                    updateUnfinishedRow(tempSizes, index - 1, unfinishedCount)
                    tempList.clear()
                    tempSizes.clear()
                    unfinishedRow.clear()
                    tempSizes.add(pop)
                    tempList.add(popFile)
                    arSum = popRatio
                    if(unfinishedCount < 1) files else files.subList(unfinishedCount, files.size)
                }
                index == dataSize -> {
                    // Last item for this page, add them as unfinished
                    unfinishedRow.addAll(tempSizes)
                    onlyNormalizeHeights(tempSizes, width / arMin)
                    val files = updateSizes(tempList, tempSizes)
                    files
                }
                else -> emptyList()
            }
        }
    }.toFlowable(BackpressureStrategy.LATEST).cachedIn(viewModelScope)

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    fun openFilesDetails(index: Int){
        openDetails.onNext(index)
    }

    fun setFolderName(folder: Folder){
        folderName.onNext(folder)
        folderId = folder.id
    }

    fun setFilter(filterBy: ListFilter){
        filter.onNext(filterBy)
    }

    fun getFilter(): ListFilter? {
        return filter.value
    }

    fun setTag(tags: FilterTags){
        this.tags.onNext(tags)
    }

    fun getTags(): FilterTags {
        return tags.value ?: FilterTags()
    }

    fun setNewPos(pos: Int){
        newFilePos.onNext(pos)
    }

    fun setListWidth(width: Int){
        widthSubject.onNext(width)
    }

    fun getURL() = settings.getServerURL()

    fun isAutoplayEnabled() = settings.getAutoPlayMode()

    fun setSheet(visible: Boolean) {
        detailsState.value?.let {
            detailsState.onNext(it.copy(sheetVisible = visible))
        }
    }

    @Synchronized
    private fun normalizeHeights(subList: List<Size>, height: Float) {
        var totalWidth = 0
        for (temp in subList) {
            val width = (height * getAspectRatio(temp)).toInt()
            totalWidth += width
            temp.width = width
            temp.height = height.toInt()
        }

        // Sometimes the total width is off by a couple of pixels, e.g. (screen: 720, total: 718)
        // Add the remaining to compensate
        val remaining = width - totalWidth
        if(remaining > 0) subList.last().width += remaining
    }

    private fun onlyNormalizeHeights(subList: List<Size>, height: Float){
        var totalWidth = 0
        for (temp in subList) {
            val width = (height * getAspectRatio(temp)).toInt()
            totalWidth += width
            temp.width = width
            temp.height = height.toInt()
        }
    }

    @Synchronized
    private fun getAspectRatio(dim: Size): Float {
        return 1.0f * dim.width / dim.height
    }

    private fun updateSizes(files: List<File>, sizes: List<Size>): List<File>{
        return sizes.mapIndexed { index, newSize ->
            val file = files[index]
            file.displayWidth = newSize.width
            file.displayHeight = newSize.height
            file
        }
    }

    /**
     * Sometimes the last items of a page can't complete a full row, when new data is added a row is completed
     * and their sizes potentially changed, this updates the size of those items. The position of the last item
     * in [tempSizes] is given by [endIndex], and [unfinishedCount] is how many items need an update.
     */
    private fun updateUnfinishedRow(tempSizes: List<Size>, endIndex: Int, unfinishedCount: Int) {
        if (unfinishedCount == 0) return
        val start = endIndex - tempSizes.size
        val rows = arrayListOf<UpdatedRow>()
        for((zi, newIndex) in (start until endIndex).withIndex()){
            rows.add(UpdatedRow(newIndex, tempSizes[zi]))
        }
        updateRows.onNext(rows)
    }

    fun setCoverFile(fileId: Long) = filesRepository.updateFolderCover(folderId, fileId)

    fun refresh() {
        forceUpdate.onNext(true)
    }

    data class Size(var width: Int, var height: Int)
    data class UpdatedRow(val pos: Int, val size: Size)

    data class ListFilter(
        val sortType: String = SortField.CREATED.field,
        val order: String = Order.DESC.oder,
    )

    data class DetailsUIState(val sheetVisible: Boolean = false)
}
