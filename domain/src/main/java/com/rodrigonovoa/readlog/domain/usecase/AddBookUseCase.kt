package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.repository.BookRepository
import javax.inject.Inject

class AddBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(
        title: String,
        author: String,
        numPages: Int,
        currentPage: Int,
    ): Result<Unit> {
        val book = Book(
            title = title.trim(),
            author = author.trim(),
            genre = "",
            releaseDate = "",
            numPages = numPages,
            currentPage = currentPage,
        )
        return runCatching { bookRepository.insertBook(book) }
    }
}
