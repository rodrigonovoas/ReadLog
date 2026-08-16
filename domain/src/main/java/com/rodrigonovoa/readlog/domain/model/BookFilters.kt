package com.rodrigonovoa.readlog.domain.model

data class BookFilters(
    val title: String? = null,
    val author: String? = null,
    val state: BookState? = null,
)
