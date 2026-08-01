package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.model.BookState
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
        state: BookState = BookState.IN_PROGRESS,
        coverUrl: String = "",
    ): Result<Unit> {
        val book = Book(
            title = title.trim(),
            author = author.trim(),
            genre = "",
            releaseDate = "",
            numPages = numPages,
            currentPage = currentPage,
            state = state,
            coverUrl = coverUrl,
        )
        return runCatching { bookRepository.insertBook(book) }
    }
}
