package com.guillermonegrete.gallery.files

import android.view.View
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.common.Field

enum class SortField(val field: String, val display: String, val id: Int) {
    FILENAME("filename", "Filename", R.id.by_name),
    CREATED("creationDate", "Creation date", R.id.by_creation),
    MODIFIED("lastModified", "Last modified", R.id.by_last_modified),

    NAME("name", "Name", View.generateViewId()),
    COUNT("count", "Count", View.generateViewId());

    companion object {
        private val values: Array<SortField> = entries.toTypedArray()

        fun fromId(id: Int): SortField {
            return values.first { it.id == id }
        }

        fun fromField(field: String?): SortField? {
            return values.firstOrNull { it.field == field }
        }

        fun toDisplayArray(list: List<SortField>) = list.map { Field(it.display, it.id) }.toTypedArray()

        val DEFAULT = FILENAME
        val DEFAULT_FOLDER = NAME
    }
}
