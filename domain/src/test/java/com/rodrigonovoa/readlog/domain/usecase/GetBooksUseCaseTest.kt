package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.fakes.FakeBookRepository
import com.rodrigonovoa.readlog.domain.model.Book
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetBooksUseCaseTest {

    private val repository = FakeBookRepository()
    private val useCase = GetBooksUseCase(repository)

    @Test
    fun `invoke returns all books from repository`() = runTest {
        val expectedBooks = listOf(
            Book(
                bookId = 1,
                title = "Cien años de soledad",
                author = "Gabriel García Márquez",
                genre = "Novel",
                releaseDate = "1967",
                numPages = 340,
                currentPage = 231,
            ),
            Book(
                bookId = 2,
                title = "Las palabras y las cosas",
                author = "Michel Foucault",
                genre = "Philosophy",
                releaseDate = "1966",
                numPages = 300,
                currentPage = 102,
            ),
        )
        repository.books = expectedBooks

        val result = useCase().first()

        assertEquals(expectedBooks, result)
    }

    @Test
    fun `invoke returns empty list when repository is empty`() = runTest {
        repository.books = emptyList()

        val result = useCase().first()

        assertEquals(emptyList<Book>(), result)
    }
}
