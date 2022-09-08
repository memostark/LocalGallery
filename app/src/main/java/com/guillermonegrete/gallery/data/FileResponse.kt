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
    @Json(name = "file_type") val type: FileType
){
    abstract fun toFile(): File

    override fun toString(): String {
        return "{url: $url, tags: $tags}"
    }
}

class ImageFileResponse(url: String, width: Int, height: Int, creationDate: Date, lastModified: Date, tags: List<Tag>):
    FileResponse(url, width, height, creationDate, lastModified, tags, FileType.Image){

    constructor(url: String, width: Int, height: Int): this(url, width, height, Date(), Date(), listOf())

    override fun toFile() = ImageFile(url, width, height, creationDate, lastModified, tags)
}

class VideoFileResponse(url: String, width: Int, height: Int, creationDate: Date, lastModified: Date, tags: List<Tag>, private val duration: Int): FileResponse(url, width, height, creationDate, lastModified, tags, FileType.Video){
    constructor(url: String, width: Int, height: Int, duration: Int): this(url, width, height, Date(), Date(), listOf(), duration )
    override fun toFile() = VideoFile(url, width, height, creationDate, lastModified, duration, tags)
}

enum class FileType{
    Image,
    Video
}