package com.guillermonegrete.gallery.files

enum class FilesSorting(val field: String, val display: String) {
    FILENAME("filename", "Filename"),
    CREATED("creationDate", "Creation date"),
    MODIFIED("lastModified", "Last modified");

    companion object {
        private val values: Array<FilesSorting> = values()

        fun fromInteger(x: Int): FilesSorting {
            return values[x]
        }

        fun toArray() = values.map { it.display }.toTypedArray()

        val DEFAULT = FILENAME
    }
}
