package com.guillermonegrete.gallery.files

import com.guillermonegrete.gallery.data.File
import com.guillermonegrete.gallery.data.ImageFile
import com.guillermonegrete.gallery.data.VideoFile

object AspectRatioComputer {

    @Synchronized
    fun normalizeHeights(subList: List<FilesListFragment.Size>, height: Float, screenWidth: Int) {
        var totalWidth = 0
        for (temp in subList) {
            val width = (height * getAspectRatio(temp)).toInt()
            totalWidth += width
            temp.width = width
            temp.height = height.toInt()
        }

        // Sometimes the total width is off by a couple of pixels, e.g. (screen: 720, total: 718)
        // Add the remaining to compensate
        val remaining = screenWidth - totalWidth
        if(remaining > 0) subList.last().width += remaining
    }

    @Synchronized
    fun getAspectRatio(dim: FilesListFragment.Size): Float {
        return 1.0f * dim.width / dim.height
    }

    fun updateSizes(files: List<File>, sizes: List<FilesListFragment.Size>): List<File>{
        return sizes.mapIndexed { index, newSize ->
            when(val oldFile = files[index]){
                is ImageFile -> ImageFile(oldFile.name, newSize.width, newSize.height)
                is VideoFile -> VideoFile(oldFile.name, newSize.width, newSize.height, oldFile.duration)
            }
        }
    }
}
