package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class GetBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    open operator fun invoke(): Flow<List<Book>> {
        return bookRepository.getAllBooks()
    }
}
