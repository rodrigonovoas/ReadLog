package com.rodrigonovoa.readlog.domain.model

data class Book(
    val bookId: Int = 0,
    val remoteId: String = "",
    val title: String,
    val author: String,
    val genre: String,
    val releaseDate: String,
    val numPages: Int,
    val currentPage: Int,
    val creationDate: Long = System.currentTimeMillis(),
    val lastModified: Long = 0L,
)
