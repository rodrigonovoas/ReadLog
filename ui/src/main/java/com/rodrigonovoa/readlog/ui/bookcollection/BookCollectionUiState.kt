package com.rodrigonovoa.readlog.ui.bookcollection

import com.rodrigonovoa.readlog.domain.model.Book

data class BookCollectionUiState(
    val books: List<Book> = emptyList(),
    val greetingResId: Int = 0,
    val userName: String = "",
    val selectedBookId: Int? = null,
)
