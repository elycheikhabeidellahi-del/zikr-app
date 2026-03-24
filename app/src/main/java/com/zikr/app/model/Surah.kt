package com.zikr.app.model

data class Surah(
    val id: String,
    val title: String,
    val text: String
) {
    val speechText: String
        get() = text.replace("\n", " ").trim()
}
