package com.rodrigonovoa.readlog.ui.bookcollection

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.usecase.GetBooksUseCase
import com.rodrigonovoa.readlog.domain.usecase.InsertMockBooksUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookCollectionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getBooksUseCase: GetBooksUseCase
    private lateinit var insertMockBooksUseCase: InsertMockBooksUseCase
    private lateinit var viewModel: BookCollectionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getBooksUseCase = mockk()
        insertMockBooksUseCase = mockk(relaxed = true)
        val booksFlow = MutableStateFlow<List<Book>>(emptyList())
        every { getBooksUseCase() } returns booksFlow
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

        coVerify { insertMockBooksUseCase() }
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

        val booksFlow = MutableStateFlow(expectedBooks)
        every { getBooksUseCase() } returns booksFlow
        // Re-create ViewModel so it subscribes to the new flow
        viewModel = BookCollectionViewModel(
            getBooksUseCase = getBooksUseCase,
            insertMockBooksUseCase = insertMockBooksUseCase,
        )
        advanceUntilIdle()

        assertEquals(expectedBooks, viewModel.books.value)
    }

    @Test
    fun `init emits empty list when no books`() = runTest {
        advanceUntilIdle()

        assertEquals(emptyList<Book>(), viewModel.books.value)
    }
}
