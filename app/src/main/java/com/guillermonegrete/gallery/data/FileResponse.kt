package com.guillermonegrete.gallery.data

import com.squareup.moshi.Json

sealed class FileResponse(val url: String, val width: Int, val height: Int, @Json(name="file_type") val type: Int){
    abstract fun toFile(): File
}

class ImageFileResponse(url: String, width: Int, height: Int): FileResponse(url, width, height, FileType.Image.value){
    override fun toFile() = ImageFile(url, width, height)
}

class VideoFileResponse(url: String, width: Int, height: Int, val duration: Int): FileResponse(url, width, height, FileType.Video.value){
    override fun toFile() = VideoFile(url, width, height, duration)
}

enum class FileType(val value: Int){
    Image(1),
    Video(2)
}