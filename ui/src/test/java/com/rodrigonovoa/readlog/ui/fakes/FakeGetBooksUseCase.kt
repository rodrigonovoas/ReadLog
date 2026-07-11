package com.rodrigonovoa.readlog.ui.fakes

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.usecase.GetBooksUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGetBooksUseCase : GetBooksUseCase(
    bookRepository = FakeBookRepository()
) {
    private val _books = MutableStateFlow<List<Book>>(emptyList())

    override fun invoke(): Flow<List<Book>> = _books

    fun emitBooks(books: List<Book>) {
        _books.value = books
    }
}
