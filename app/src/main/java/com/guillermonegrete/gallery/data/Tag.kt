package com.guillermonegrete.gallery.data

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Tag(
    val name: String,
    val creationDate: Date = Date(),
    val id: Long = 0L,
): Parcelable {

    @Json(name = "tag_type") var type: TagType? = null
    /**
     * The amount of files in the folder that have this tag applied.
     */
    var count: Long = 0L

    private companion object : Parceler<Tag> {

        override fun Tag.write(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeLong(creationDate.time)
            parcel.writeLong(id)
            parcel.writeString(type?.name)
        }

        override fun create(parcel: Parcel)
            = Tag(parcel.readString() ?: "", Date(parcel.readLong()),  parcel.readLong())
                .apply {
                    val typeText = parcel.readString()
                    type = if (typeText != null) TagType.valueOf(typeText) else null
                    count = parcel.readLong()
                }
    }
}

enum class TagType{
    Folder,
    File,
}

