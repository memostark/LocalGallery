package com.guillermonegrete.gallery.data

import com.squareup.moshi.Json
import java.util.*

sealed class FileResponse(
    val url: String,
    val width: Int,
    val height: Int,
    val creationDate: Date,
    val lastModified: Date,
    @Json(name = "file_type") val type: FileType
){
    abstract fun toFile(): File
}

class ImageFileResponse(url: String, width: Int, height: Int, creationDate: Date, lastModified: Date): FileResponse(url, width, height, creationDate, lastModified, FileType.Image){
    override fun toFile() = ImageFile(url, width, height, creationDate, lastModified)
}

class VideoFileResponse(url: String, width: Int, height: Int, creationDate: Date, lastModified: Date, private val duration: Int): FileResponse(url, width, height, creationDate, lastModified, FileType.Video){
    override fun toFile() = VideoFile(url, width, height, creationDate, lastModified, duration)
}

enum class FileType{
    Image,
    Video
}