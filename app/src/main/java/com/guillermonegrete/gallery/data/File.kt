package com.guillermonegrete.gallery.data

sealed class File(
    val name:String,
    val width: Int,
    val height: Int,
)

class ImageFile(name: String, width: Int = 0, height: Int = 0): File(name, width, height){
    // To make testing easier when comparing and simulate a data class
    override fun equals(other: Any?) =
        if(other is ImageFile) name == other.name && width == other.width && height == other.height else false

    override fun hashCode() = javaClass.hashCode()
}
class VideoFile(name: String, width: Int = 0, height: Int = 0, val duration: Int): File(name, width, height)