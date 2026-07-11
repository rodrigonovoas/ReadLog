package com.rodrigonovoa.readlog.domain.fakes

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeBookRepository : BookRepository {

    var books: List<Book> = emptyList()
    var insertedBooks = mutableListOf<Book>()
    var booksCount: Int = 0

    override fun getAllBooks(): Flow<List<Book>> {
        return flowOf(books)
    }

    override suspend fun insertBook(book: Book) {
        insertedBooks.add(book)
    }

    override suspend fun getBooksCount(): Int {
        return booksCount
    }
}
