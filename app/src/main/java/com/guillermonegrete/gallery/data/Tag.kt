package com.guillermonegrete.gallery.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Tag(val name: String, val creationDate: Date, val id: Int): Parcelable