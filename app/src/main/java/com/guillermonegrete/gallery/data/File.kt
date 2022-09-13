package com.guillermonegrete.gallery.data

import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.io.File as JavaFile

sealed class File(
    val name: String, // actually an URL with name of the file
    var width: Int,
    var height: Int,
    val creationDate: Date,
    val lastModified: Date,
    val tags: List<Tag>,
    val id: Long,
) {
    val filename: String =  try { JavaFile(URL(name).path).name ?: name } catch (e: Exception) { name } // Maybe it will be better if backend gives the filename
    val sizeText: String = "${width}x$height"
    private val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    val creationText: CharSequence = formatter.format(creationDate)
    val modifiedText: CharSequence = formatter.format(lastModified)
}

class ImageFile(
    name: String,
    width: Int = 0,
    height: Int = 0,
    creationDate: Date = Date(),
    lastModified: Date = Date(),
    tags: List<Tag> = listOf(),
    id: Long,
): File(name, width, height, creationDate, lastModified, tags, id){
    // To make testing easier when comparing and simulate a data class
    override fun equals(other: Any?) =
        if(other is ImageFile) name == other.name && width == other.width && height == other.height else false

    override fun hashCode() = javaClass.hashCode()
}
class VideoFile(
    name: String,
    width: Int = 0,
    height: Int = 0,
    creationDate: Date,
    lastModified: Date,
    val duration: Int,
    tags: List<Tag> = listOf(),
    id: Long,
): File(name, width, height, creationDate, lastModified, tags, id)