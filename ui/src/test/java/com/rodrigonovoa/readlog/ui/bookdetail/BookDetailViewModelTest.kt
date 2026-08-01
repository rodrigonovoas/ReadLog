package com.rodrigonovoa.readlog.ui.bookdetail

import androidx.lifecycle.SavedStateHandle
import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.model.BookState
import com.rodrigonovoa.readlog.domain.model.Session
import com.rodrigonovoa.readlog.domain.usecase.GetAnnotationsForSessionUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetBookByIdUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetSessionsForBookUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class BookDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getBookByIdUseCase: GetBookByIdUseCase
    private lateinit var getSessionsForBookUseCase: GetSessionsForBookUseCase
    private lateinit var getAnnotationsForSessionUseCase: GetAnnotationsForSessionUseCase
    private lateinit var savedStateHandle: SavedStateHandle

    private val bookId = 7

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getBookByIdUseCase = mockk()
        getSessionsForBookUseCase = mockk()
        getAnnotationsForSessionUseCase = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle(mapOf("bookId" to bookId))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads book details into state`() = runTest {
        val creationDate = daysAgo(5)
        val book = book(
            title = "Cien años de soledad",
            author = "Gabriel García Márquez",
            state = BookState.IN_PROGRESS,
            creationDate = creationDate,
            coverUrl = "https://example.com/cover.jpg",
        )
        coEvery { getBookByIdUseCase(bookId) } returns book
        coEvery { getSessionsForBookUseCase(bookId) } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Cien años de soledad", viewModel.uiState.value.bookTitle)
        assertEquals("Gabriel García Márquez", viewModel.uiState.value.bookAuthor)
        assertEquals(BookState.IN_PROGRESS, viewModel.uiState.value.bookState)
        assertEquals("https://example.com/cover.jpg", viewModel.uiState.value.coverUrl)
    }

    @Test
    fun `days reading for in-progress book counts until today`() = runTest {
        val creationDate = daysAgo(5)
        val book = book(state = BookState.IN_PROGRESS, creationDate = creationDate)
        coEvery { getBookByIdUseCase(bookId) } returns book
        coEvery { getSessionsForBookUseCase(bookId) } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(6, viewModel.uiState.value.daysReadingCount)
    }

    @Test
    fun `days reading for completed book counts until statusDate`() = runTest {
        val creationDate = daysAgo(10)
        val statusDate = daysAgo(3)
        val book = book(
            state = BookState.COMPLETED,
            creationDate = creationDate,
            statusDate = statusDate,
        )
        coEvery { getBookByIdUseCase(bookId) } returns book
        coEvery { getSessionsForBookUseCase(bookId) } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(8, viewModel.uiState.value.daysReadingCount)
    }

    @Test
    fun `days reading for dropped book counts until statusDate`() = runTest {
        val creationDate = daysAgo(10)
        val statusDate = daysAgo(3)
        val book = book(
            state = BookState.DROPPED,
            creationDate = creationDate,
            statusDate = statusDate,
        )
        coEvery { getBookByIdUseCase(bookId) } returns book
        coEvery { getSessionsForBookUseCase(bookId) } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(8, viewModel.uiState.value.daysReadingCount)
    }

    @Test
    fun `days reading for paused book counts until statusDate`() = runTest {
        val creationDate = daysAgo(10)
        val statusDate = daysAgo(3)
        val book = book(
            state = BookState.PAUSED,
            creationDate = creationDate,
            statusDate = statusDate,
        )
        coEvery { getBookByIdUseCase(bookId) } returns book
        coEvery { getSessionsForBookUseCase(bookId) } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(8, viewModel.uiState.value.daysReadingCount)
    }

    @Test
    fun `days reading for completed book without statusDate falls back to today`() = runTest {
        val creationDate = daysAgo(5)
        val book = book(
            state = BookState.COMPLETED,
            creationDate = creationDate,
            statusDate = null,
        )
        coEvery { getBookByIdUseCase(bookId) } returns book
        coEvery { getSessionsForBookUseCase(bookId) } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(6, viewModel.uiState.value.daysReadingCount)
    }

    @Test
    fun `sessions count and total time are computed from sessions`() = runTest {
        val book = book(state = BookState.IN_PROGRESS)
        val sessions = listOf(
            Session(sessionId = 1, bookId = bookId, time = 120L),
            Session(sessionId = 2, bookId = bookId, time = 180L),
        )
        coEvery { getBookByIdUseCase(bookId) } returns book
        coEvery { getSessionsForBookUseCase(bookId) } returns flowOf(sessions)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.sessionsCount)
        assertEquals("5 m.", viewModel.uiState.value.totalTimeLabel)
    }

    private fun createViewModel(): BookDetailViewModel {
        return BookDetailViewModel(
            getBookByIdUseCase = getBookByIdUseCase,
            getSessionsForBookUseCase = getSessionsForBookUseCase,
            getAnnotationsForSessionUseCase = getAnnotationsForSessionUseCase,
            savedStateHandle = savedStateHandle,
        )
    }

    private fun book(
        title: String = "Title",
        author: String = "Author",
        state: BookState = BookState.IN_PROGRESS,
        creationDate: Long = daysAgo(0),
        statusDate: Long? = null,
        coverUrl: String = "",
    ): Book {
        return Book(
            bookId = bookId,
            title = title,
            author = author,
            genre = "",
            releaseDate = "",
            numPages = 100,
            currentPage = 0,
            state = state,
            creationDate = creationDate,
            statusDate = statusDate,
            coverUrl = coverUrl,
        )
    }

    private fun daysAgo(days: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, -days)
        }.timeInMillis
    }
}
