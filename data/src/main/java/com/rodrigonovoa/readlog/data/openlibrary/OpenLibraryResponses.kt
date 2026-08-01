package com.rodrigonovoa.readlog.data.openlibrary

import com.google.gson.annotations.SerializedName

data class OpenLibraryIsbnResponse(
    val title: String?,
    val authors: List<OpenLibraryAuthorRef>?,
    @SerializedName("number_of_pages") val numberOfPages: Int?,
    @SerializedName("publish_date") val publishDate: String?,
    val subjects: List<String>?,
    val covers: List<Int>? = null,
)

data class OpenLibraryAuthorRef(
    val key: String?,
)

data class OpenLibraryAuthorResponse(
    val name: String?,
)
