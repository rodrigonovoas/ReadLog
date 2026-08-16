package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.model.BookFilters
import com.rodrigonovoa.readlog.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

open class GetBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    open operator fun invoke(filters: BookFilters = BookFilters()): Flow<List<Book>> {
        return bookRepository.getAllBooks().map { books ->
            books.filter { book ->
                val titleMatch = filters.title.isNullOrBlank() ||
                    book.title.contains(filters.title, ignoreCase = true)
                val authorMatch = filters.author.isNullOrBlank() ||
                    book.author.contains(filters.author, ignoreCase = true)
                val stateMatch = filters.state == null || book.state == filters.state
                titleMatch && authorMatch && stateMatch
            }
        }
    }
}
