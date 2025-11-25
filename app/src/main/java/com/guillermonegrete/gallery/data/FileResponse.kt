package com.guillermonegrete.gallery.data

import com.squareup.moshi.Json
import java.util.*

sealed class FileResponse(
    val url: String,
    val filename: String,
    val width: Int,
    val height: Int,
    val creationDate: Date,
    val lastModified: Date,
    val tags: List<Tag> = listOf(),
    val folder: Folder? = null,
    val id: Long,
    @property:Json(name = "file_type") val type: FileType
){
    abstract fun toFile(): File

    override fun toString(): String {
        return "{url: $url, tags: $tags}"
    }
}

class ImageFileResponse(
    url: String,
    filename: String,
    width: Int,
    height: Int,
    creationDate: Date,
    lastModified: Date,
    tags: List<Tag>,
    folder: Folder?,
    id: Long,
): FileResponse(url, filename, width, height, creationDate, lastModified, tags, folder, id, FileType.Image){

    // Constructor for testing
    constructor(url: String, width: Int, height: Int): this(url, "", width, height, Date(), Date(), listOf(), null, 0)

    override fun toFile() = ImageFile(url, filename, width, height, 0, 0, creationDate, lastModified, tags, folder, id)
}

class VideoFileResponse(
    url: String,
    filename: String,
    width: Int,
    height: Int,
    creationDate: Date,
    lastModified: Date,
    tags: List<Tag>,
    folder: Folder?,
    private val duration: Int,
    id: Long,
): FileResponse(url, filename, width, height, creationDate, lastModified, tags, folder, id, FileType.Video){
    // Constructor for testing
    constructor(url: String, width: Int, height: Int, duration: Int): this(url, "", width, height, Date(), Date(), listOf(), null, duration, 0)
    override fun toFile() = VideoFile(url, filename, width, height, 0, 0, creationDate, lastModified, duration, tags, folder, id)
}

enum class FileType{
    Image,
    Video
}

data class FileInfoResponse(
    @property:Json(name = "thumbnail_sizes")
    val thumbnailSizes: Map<String, Int>
)
