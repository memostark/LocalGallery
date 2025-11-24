package com.guillermonegrete.gallery.data

import java.util.*

sealed class File(
    val name: String, // actually an URL with name of the file
    val filename: String,
    val width: Int,
    val height: Int,
    /**
     * The width in pixels used to display the image in the gallery.
     * This is calculated after the arrangement of the files is done.
     */
    var displayWidth: Int,
    /**
     * Same as [displayWidth].
     */
    var displayHeight: Int,
    val creationDate: Date,
    val lastModified: Date,
    var tags: List<Tag>,
    val folder: Folder?,
    val id: Long,
) {
    val sizeText by lazy { "${width}x$height" }

    var thumbnail: String? = null
}

class ImageFile(
    name: String,
    filename: String = "",
    width: Int = 0,
    height: Int = 0,
    displayWidth: Int = 0,
    displayHeight: Int = 0,
    creationDate: Date = Date(),
    lastModified: Date = Date(),
    tags: List<Tag> = listOf(),
    folder: Folder? = null,
    id: Long,
): File(name, filename, width, height, displayWidth, displayHeight, creationDate, lastModified, tags, folder, id){
    // To make testing easier when comparing and simulate a data class
    override fun equals(other: Any?) =
        if(other is ImageFile) name == other.name && displayWidth == other.displayWidth && displayHeight == other.displayHeight else false

    override fun hashCode() = javaClass.hashCode()
}
class VideoFile(
    name: String,
    filename: String = "",
    width: Int = 0,
    height: Int = 0,
    displayWidth: Int = 0,
    displayHeight: Int = 0,
    creationDate: Date,
    lastModified: Date,
    val duration: Int,
    tags: List<Tag> = listOf(),
    folder: Folder? = null,
    id: Long,
): File(name, filename, width, height, displayWidth, displayHeight, creationDate, lastModified, tags, folder, id)