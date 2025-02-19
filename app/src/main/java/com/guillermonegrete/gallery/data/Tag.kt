package com.guillermonegrete.gallery.data

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Tag(
    val name: String,
    val creationDate: Date,
    val id: Long,
): Parcelable {
    /**
     * The amount of files in the folder that have this tag applied.
     */
    var count: Long = 0L

    private companion object : Parceler<Tag> {

        override fun Tag.write(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeLong(creationDate.time)
            parcel.writeLong(id)
            parcel.writeLong(count)
        }

        override fun create(parcel: Parcel)
            = Tag(parcel.readString() ?: "", Date(parcel.readLong()), parcel.readLong())
                .apply { count = parcel.readLong() }
    }
}
