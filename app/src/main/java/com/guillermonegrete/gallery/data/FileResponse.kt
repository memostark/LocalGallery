package com.guillermonegrete.gallery.data

import com.squareup.moshi.Json
import java.util.*

sealed class FileResponse(
    val url: String,
    val width: Int,
    val height: Int,
    val creationDate: Date,
    val lastModified: Date,
    val tags: List<Tag> = listOf(),
    val id: Long,
    @Json(name = "file_type") val type: FileType
){
    abstract fun toFile(): File

    override fun toString(): String {
        return "{url: $url, tags: $tags}"
    }
}

class ImageFileResponse(
    url: String,
    width: Int,
    height: Int,
    creationDate: Date,
    lastModified: Date,
    tags: List<Tag>,
    id: Long,
): FileResponse(url, width, height, creationDate, lastModified, tags, id, FileType.Image){

    constructor(url: String, width: Int, height: Int): this(url, width, height, Date(), Date(), listOf(), 0)

    override fun toFile() = ImageFile(url, width, height, 0, 0, creationDate, lastModified, tags, id)
}

class VideoFileResponse(
    url: String,
    width: Int,
    height: Int,
    creationDate: Date,
    lastModified: Date,
    tags: List<Tag>,
    private val duration: Int,
    id: Long,
): FileResponse(url, width, height, creationDate, lastModified, tags, id, FileType.Video){
    constructor(url: String, width: Int, height: Int, duration: Int): this(url, width, height, Date(), Date(), listOf(), duration, 0)
    override fun toFile() = VideoFile(url, width, height, 0, 0, creationDate, lastModified, duration, tags, id)
}

enum class FileType{
    Image,
    Video
}
