package com.guillermonegrete.gallery.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.flatMap
import androidx.paging.map
import androidx.paging.rxjava2.cachedIn
import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.ImageFile
import com.guillermonegrete.gallery.data.VideoFile
import com.guillermonegrete.gallery.data.source.FilesRepository
import com.guillermonegrete.gallery.data.source.SettingsRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class FilesViewModel @Inject constructor(
    settings: SettingsRepository,
    private val filesRepository: FilesRepository,
): ViewModel() {

    val openDetails: Subject<Int> = PublishSubject.create()
    val updateRows: Subject<List<UpdatedRow>> = PublishSubject.create()

    private val folderName: Subject<String> = PublishSubject.create()
    private val filter: Subject<String> = BehaviorSubject.createDefault("")

    var dataSize = 0

    private val arMin = 2.0f
    private val arMax = 3.0f

    var width = 0

    var cachedFileList: Flowable<PagingData<File>> = filter.distinctUntilChanged().switchMap { filter ->
        folderName.distinctUntilChanged().switchMap { folder ->
            dataSize = 0
            val finalFilter = if(filter.isEmpty()) null else filter
            filesRepository.getPagedFiles(folder, finalFilter).toObservable()
        }
    }.map { pagingData ->
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
                    val pop = tempSizes.removeLast()
                    val popFile = tempList.removeLast()
                    arSum -= getAspectRatio(pop)
                    normalizeHeights(tempSizes, width / arSum)
                    val files = updateSizes(tempList, tempSizes)
                    val unfinishedCount = unfinishedRow.size
                    updateUnfinishedRow(tempSizes, index, unfinishedCount)
                    tempList.clear()
                    tempSizes.clear()
                    unfinishedRow.clear()
                    tempSizes.add(pop)
                    tempList.add(popFile)
                    arSum = getAspectRatio(pop)
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

    init {
        val url = settings.getServerURL()
        filesRepository.updateRepoURL(url)
    }

    fun openFilesDetails(index: Int){
        openDetails.onNext(index)
    }

    fun setFolderName(name: String){
        folderName.onNext(name)
    }

    fun setFilter(filterBy: String){
        filter.onNext(filterBy)
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
            when(val oldFile = files[index]){
                is ImageFile -> ImageFile(oldFile.name, newSize.width, newSize.height, oldFile.creationDate, oldFile.lastModified)
                is VideoFile -> VideoFile(oldFile.name, newSize.width, newSize.height, oldFile.creationDate, oldFile.lastModified, oldFile.duration)
            }
        }
    }

    /**
     * Sometimes the last items of a page can't complete a full row, when new data is added a row is completed
     * and their sizes potentially changed, this updates the size of those items.
     */
    private fun updateUnfinishedRow(tempSizes: List<Size>, itemIndex: Int, unfinishedCount: Int) {
        val i = itemIndex - tempSizes.size
        val rows = arrayListOf<UpdatedRow>()
        for((zi, newIndex) in ((i + 1)..(i + unfinishedCount)).withIndex()){
            rows.add(UpdatedRow(newIndex - 1,  tempSizes[zi]))
        }
        updateRows.onNext(rows)
    }

    data class Size(var width: Int, var height: Int)
    data class UpdatedRow(val pos: Int, val size: Size)
}
