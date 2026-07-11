package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.repository.BookRepository
import javax.inject.Inject

/**
 * Temporary use case to populate the database with mock books.
 * Will be removed once the app has real book creation flows.
 */
open class InsertMockBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    open suspend operator fun invoke() {
        if (bookRepository.getBooksCount() > 0) return

        val mockBooks = listOf(
            Book(
                title = "Cien años de soledad",
                author = "Gabriel García Márquez",
                genre = "Novel",
                releaseDate = "1967",
                numPages = 340,
                currentPage = 231,
            ),
            Book(
                title = "Las palabras y las cosas",
                author = "Michel Foucault",
                genre = "Philosophy",
                releaseDate = "1966",
                numPages = 300,
                currentPage = 102,
            ),
            Book(
                title = "El nombre del viento",
                author = "Patrick Rothfuss",
                genre = "Fantasy",
                releaseDate = "2007",
                numPages = 662,
                currentPage = 80,
            ),
        )

        mockBooks.forEach { book ->
            bookRepository.insertBook(book)
        }
    }
}
