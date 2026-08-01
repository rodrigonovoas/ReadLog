package com.rodrigonovoa.readlog.domain.model

data class BookMetadata(
    val title: String,
    val author: String,
    val genre: String,
    val releaseDate: String,
    val numPages: Int?,
    val coverUrl: String = "",
)
