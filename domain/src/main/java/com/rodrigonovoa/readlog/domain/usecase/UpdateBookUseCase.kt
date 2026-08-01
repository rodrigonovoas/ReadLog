package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.model.BookState
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
        state: BookState,
    ): Result<Unit> {
        val updated = original.copy(
            title = title.trim(),
            author = author.trim(),
            numPages = numPages,
            currentPage = currentPage,
            state = state,
            statusDate = resolveStatusDate(original, state),
        )
        return runCatching { bookRepository.updateBook(updated) }
    }

    private fun resolveStatusDate(original: Book, newState: BookState): Long? {
        return when (newState) {
            BookState.COMPLETED,
            BookState.DROPPED,
            BookState.PAUSED -> original.statusDate ?: System.currentTimeMillis()
            BookState.IN_PROGRESS -> null
        }
    }
}
