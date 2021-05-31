package com.guillermonegrete.gallery.data

data class File(
    val name:String,
    val type:String,
    val width: Int = 0,
    val height: Int = 0,
)