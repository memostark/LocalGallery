package com.guillermonegrete.gallery.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Tag(
    val name: String,
    val creationDate: Date,
    val id: Long,
    /**
     * The amount of files in the folder that have this tag applied.
     */
    val count: Long = 0,
): Parcelable


data class TagCount(
    val name: String,
    val creationDate: Date,
    /**
     * The amount of files in the folder that have this tag applied.
     */
    val count: Long,
    val id: Long,
)
