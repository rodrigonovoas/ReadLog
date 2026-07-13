package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.repository.BookRepository
import javax.inject.Inject

class UpdateBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(
        original: Book,
        title: String,
        author: String,
        numPages: Int,
        currentPage: Int,
    ): Result<Unit> {
        val updated = original.copy(
            title = title.trim(),
            author = author.trim(),
            numPages = numPages,
            currentPage = currentPage,
        )
        return runCatching { bookRepository.updateBook(updated) }
    }
}
