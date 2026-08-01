package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.model.BookState
import com.rodrigonovoa.readlog.domain.repository.BookRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateBookUseCaseTest {

    private val bookRepository: BookRepository = mockk(relaxed = true)
    private val useCase = UpdateBookUseCase(bookRepository)

    @Test
    fun `invoke sets statusDate when transitioning to COMPLETED`() = runTest {
        val original = book(state = BookState.IN_PROGRESS, statusDate = null)

        useCase(original, "Title", "Author", 100, 50, BookState.COMPLETED)

        coVerify {
            bookRepository.updateBook(
                match { updated ->
                    updated.state == BookState.COMPLETED && updated.statusDate != null
                }
            )
        }
    }

    @Test
    fun `invoke preserves existing statusDate when editing a completed book`() = runTest {
        val originalStatusDate = 12345678L
        val original = book(state = BookState.COMPLETED, statusDate = originalStatusDate)

        useCase(original, "Updated Title", "Author", 100, 50, BookState.COMPLETED)

        coVerify {
            bookRepository.updateBook(
                match { updated ->
                    updated.state == BookState.COMPLETED && updated.statusDate == originalStatusDate
                }
            )
        }
    }

    @Test
    fun `invoke sets statusDate when transitioning to DROPPED`() = runTest {
        val original = book(state = BookState.IN_PROGRESS, statusDate = null)

        useCase(original, "Title", "Author", 100, 50, BookState.DROPPED)

        coVerify {
            bookRepository.updateBook(
                match { updated ->
                    updated.state == BookState.DROPPED && updated.statusDate != null
                }
            )
        }
    }

    @Test
    fun `invoke sets statusDate when transitioning to PAUSED`() = runTest {
        val original = book(state = BookState.IN_PROGRESS, statusDate = null)

        useCase(original, "Title", "Author", 100, 50, BookState.PAUSED)

        coVerify {
            bookRepository.updateBook(
                match { updated ->
                    updated.state == BookState.PAUSED && updated.statusDate != null
                }
            )
        }
    }

    @Test
    fun `invoke clears statusDate when transitioning back to IN_PROGRESS`() = runTest {
        val original = book(state = BookState.COMPLETED, statusDate = 12345678L)

        useCase(original, "Title", "Author", 100, 50, BookState.IN_PROGRESS)

        coVerify {
            bookRepository.updateBook(
                match { updated ->
                    updated.state == BookState.IN_PROGRESS && updated.statusDate == null
                }
            )
        }
    }

    @Test
    fun `invoke preserves other fields and trims title and author`() = runTest {
        val original = book(
            bookId = 1,
            title = "Old Title",
            author = "Old Author",
            numPages = 200,
            currentPage = 100,
            state = BookState.IN_PROGRESS,
            statusDate = null,
        )

        useCase(original, "  New Title  ", "  New Author  ", 300, 150, BookState.PAUSED)

        coVerify {
            bookRepository.updateBook(
                match { updated ->
                    updated.bookId == 1 &&
                        updated.title == "New Title" &&
                        updated.author == "New Author" &&
                        updated.numPages == 300 &&
                        updated.currentPage == 150 &&
                        updated.state == BookState.PAUSED &&
                        updated.statusDate != null
                }
            )
        }
    }

    @Test
    fun `invoke returns repository result`() = runTest {
        val original = book(state = BookState.IN_PROGRESS)

        val result = useCase(original, "Title", "Author", 100, 50, BookState.IN_PROGRESS)

        assertEquals(Result.success(Unit), result)
    }

    private fun book(
        bookId: Int = 0,
        title: String = "Title",
        author: String = "Author",
        numPages: Int = 100,
        currentPage: Int = 0,
        state: BookState = BookState.IN_PROGRESS,
        statusDate: Long? = null,
    ): Book {
        return Book(
            bookId = bookId,
            title = title,
            author = author,
            genre = "",
            releaseDate = "",
            numPages = numPages,
            currentPage = currentPage,
            state = state,
            statusDate = statusDate,
        )
    }
}
