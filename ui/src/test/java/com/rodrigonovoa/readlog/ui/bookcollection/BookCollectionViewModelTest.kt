package com.rodrigonovoa.readlog.ui.bookcollection

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.ui.fakes.FakeGetBooksUseCase
import com.rodrigonovoa.readlog.ui.fakes.FakeInsertMockBooksUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookCollectionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getBooksUseCase: FakeGetBooksUseCase
    private lateinit var insertMockBooksUseCase: FakeInsertMockBooksUseCase
    private lateinit var viewModel: BookCollectionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getBooksUseCase = FakeGetBooksUseCase()
        insertMockBooksUseCase = FakeInsertMockBooksUseCase()
        viewModel = BookCollectionViewModel(
            getBooksUseCase = getBooksUseCase,
            insertMockBooksUseCase = insertMockBooksUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init calls insert mock books use case`() = runTest {
        advanceUntilIdle()

        assertTrue(insertMockBooksUseCase.invoked)
    }

    @Test
    fun `init collects books from get books use case`() = runTest {
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

        getBooksUseCase.emitBooks(expectedBooks)
        advanceUntilIdle()

        assertEquals(expectedBooks, viewModel.books.value)
    }

    @Test
    fun `init emits empty list when no books`() = runTest {
        advanceUntilIdle()

        assertEquals(emptyList<Book>(), viewModel.books.value)
    }
}
