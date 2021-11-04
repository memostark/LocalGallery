package com.guillermonegrete.gallery.data

import java.text.SimpleDateFormat
import java.util.*

sealed class File(
    val name:String,
    val width: Int,
    val height: Int,
    val creationDate: Date,
    val lastModified: Date
) {
    val sizeText: String = "${width}x$height"
    val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    val creationText: CharSequence = formatter.format(creationDate)
    val modifiedText: CharSequence = formatter.format(lastModified)
}

class ImageFile(name: String, width: Int = 0, height: Int = 0, creationDate: Date, lastModified: Date): File(name, width, height, creationDate, lastModified){
    // To make testing easier when comparing and simulate a data class
    override fun equals(other: Any?) =
        if(other is ImageFile) name == other.name && width == other.width && height == other.height else false

    override fun hashCode() = javaClass.hashCode()
}
class VideoFile(name: String, width: Int = 0, height: Int = 0, creationDate: Date, lastModified: Date, val duration: Int): File(name, width, height, creationDate, lastModified)