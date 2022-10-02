package com.guillermonegrete.gallery.files

import com.guillermonegrete.gallery.common.Field

enum class SortField(val field: String, val display: String) {
    FILENAME("filename", "Filename"),
    CREATED("creationDate", "Creation date"),
    MODIFIED("lastModified", "Last modified"),

    NAME("name", "Name"),
    COUNT("count", "Count");

    companion object {
        private val values: Array<SortField> = values()

        fun fromInteger(x: Int): SortField {
            return values[x]
        }

        fun toDisplayArray(list: List<SortField>) = list.map { Field(it.display, it.ordinal) }.toTypedArray()

        val DEFAULT = FILENAME
        val DEFAULT_FOLDER = NAME
    }
}
