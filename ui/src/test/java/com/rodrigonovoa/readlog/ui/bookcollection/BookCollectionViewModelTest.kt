package com.rodrigonovoa.readlog.ui.bookcollection

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.model.User
import com.rodrigonovoa.readlog.domain.usecase.GetBooksUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetCurrentUserUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetTimeOfDayUseCase
import com.rodrigonovoa.readlog.domain.usecase.InsertMockBooksUseCase
import com.rodrigonovoa.readlog.domain.usecase.TimeOfDay
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
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var getTimeOfDayUseCase: GetTimeOfDayUseCase
    private lateinit var viewModel: BookCollectionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getBooksUseCase = mockk()
        insertMockBooksUseCase = mockk(relaxed = true)
        getCurrentUserUseCase = mockk()
        getTimeOfDayUseCase = mockk()
        val booksFlow = MutableStateFlow<List<Book>>(emptyList())
        every { getBooksUseCase() } returns booksFlow
        every { getCurrentUserUseCase() } returns null
        every { getTimeOfDayUseCase() } returns TimeOfDay.AFTERNOON
        viewModel = BookCollectionViewModel(
            getBooksUseCase = getBooksUseCase,
            insertMockBooksUseCase = insertMockBooksUseCase,
            getCurrentUserUseCase = getCurrentUserUseCase,
            getTimeOfDayUseCase = getTimeOfDayUseCase,
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
            getCurrentUserUseCase = getCurrentUserUseCase,
            getTimeOfDayUseCase = getTimeOfDayUseCase,
        )
        advanceUntilIdle()

        assertEquals(expectedBooks, viewModel.uiState.value.books)
    }

    @Test
    fun `init emits empty list when no books`() = runTest {
        advanceUntilIdle()

        assertEquals(emptyList<Book>(), viewModel.uiState.value.books)
    }

    @Test
    fun `greeting uses first name only from displayName`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid", "test@test.com", "Rodrigo Novoa Salgado")
        every { getTimeOfDayUseCase() } returns TimeOfDay.AFTERNOON
        val booksFlow = MutableStateFlow<List<Book>>(emptyList())
        every { getBooksUseCase() } returns booksFlow

        viewModel = BookCollectionViewModel(
            getBooksUseCase = getBooksUseCase,
            insertMockBooksUseCase = insertMockBooksUseCase,
            getCurrentUserUseCase = getCurrentUserUseCase,
            getTimeOfDayUseCase = getTimeOfDayUseCase,
        )
        advanceUntilIdle()

        assertEquals("Rodrigo", viewModel.uiState.value.userName)
    }

    @Test
    fun `greeting falls back to reader when no user is signed in`() = runTest {
        every { getCurrentUserUseCase() } returns null
        every { getTimeOfDayUseCase() } returns TimeOfDay.MORNING
        val booksFlow = MutableStateFlow<List<Book>>(emptyList())
        every { getBooksUseCase() } returns booksFlow

        viewModel = BookCollectionViewModel(
            getBooksUseCase = getBooksUseCase,
            insertMockBooksUseCase = insertMockBooksUseCase,
            getCurrentUserUseCase = getCurrentUserUseCase,
            getTimeOfDayUseCase = getTimeOfDayUseCase,
        )
        advanceUntilIdle()

        assertEquals("reader", viewModel.uiState.value.userName)
    }
}
