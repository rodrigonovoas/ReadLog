package com.rodrigonovoa.readlog.ui.bookcollection

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.usecase.DeleteBookUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetBooksUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetTimeOfDayUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetUserDisplayNameUseCase
import com.rodrigonovoa.readlog.domain.usecase.TimeOfDay
import io.mockk.coEvery
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
    private lateinit var getUserDisplayNameUseCase: GetUserDisplayNameUseCase
    private lateinit var getTimeOfDayUseCase: GetTimeOfDayUseCase
    private lateinit var deleteBookUseCase: DeleteBookUseCase
    private lateinit var viewModel: BookCollectionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getBooksUseCase = mockk()
        getUserDisplayNameUseCase = mockk()
        getTimeOfDayUseCase = mockk()
        deleteBookUseCase = mockk(relaxed = true)
        val booksFlow = MutableStateFlow<List<Book>>(emptyList())
        every { getBooksUseCase() } returns booksFlow
        every { getUserDisplayNameUseCase() } returns "reader"
        every { getTimeOfDayUseCase() } returns TimeOfDay.AFTERNOON
        viewModel = BookCollectionViewModel(
            getBooksUseCase = getBooksUseCase,
            getUserDisplayNameUseCase = getUserDisplayNameUseCase,
            getTimeOfDayUseCase = getTimeOfDayUseCase,
            deleteBookUseCase = deleteBookUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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
            getUserDisplayNameUseCase = getUserDisplayNameUseCase,
            getTimeOfDayUseCase = getTimeOfDayUseCase,
            deleteBookUseCase = deleteBookUseCase,
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
    fun `greeting uses the name provided by getUserDisplayNameUseCase`() = runTest {
        every { getUserDisplayNameUseCase() } returns "Rodrigo"
        every { getTimeOfDayUseCase() } returns TimeOfDay.AFTERNOON
        val booksFlow = MutableStateFlow<List<Book>>(emptyList())
        every { getBooksUseCase() } returns booksFlow

        viewModel = BookCollectionViewModel(
            getBooksUseCase = getBooksUseCase,
            getUserDisplayNameUseCase = getUserDisplayNameUseCase,
            getTimeOfDayUseCase = getTimeOfDayUseCase,
            deleteBookUseCase = deleteBookUseCase,
        )
        advanceUntilIdle()

        assertEquals("Rodrigo", viewModel.uiState.value.userName)
    }

    @Test
    fun `greeting falls back to reader when no user is signed in`() = runTest {
        every { getUserDisplayNameUseCase() } returns "reader"
        every { getTimeOfDayUseCase() } returns TimeOfDay.MORNING
        val booksFlow = MutableStateFlow<List<Book>>(emptyList())
        every { getBooksUseCase() } returns booksFlow

        viewModel = BookCollectionViewModel(
            getBooksUseCase = getBooksUseCase,
            getUserDisplayNameUseCase = getUserDisplayNameUseCase,
            getTimeOfDayUseCase = getTimeOfDayUseCase,
            deleteBookUseCase = deleteBookUseCase,
        )
        advanceUntilIdle()

        assertEquals("reader", viewModel.uiState.value.userName)
    }

    @Test
    fun `onEditIconClick sets activeDialog with EDIT type`() = runTest {
        val book = Book(
            bookId = 1,
            title = "Cien años de soledad",
            author = "Author",
            genre = "Novel",
            releaseDate = "2020",
            numPages = 100,
            currentPage = 50,
        )
        val booksFlow = MutableStateFlow(listOf(book))
        every { getBooksUseCase() } returns booksFlow
        viewModel = BookCollectionViewModel(
            getBooksUseCase = getBooksUseCase,
            getUserDisplayNameUseCase = getUserDisplayNameUseCase,
            getTimeOfDayUseCase = getTimeOfDayUseCase,
            deleteBookUseCase = deleteBookUseCase,
        )
        advanceUntilIdle()

        viewModel.onEditIconClick(1)

        assertEquals(
            BookDialogState(bookId = 1, bookTitle = "Cien años de soledad", type = BookDialogType.EDIT),
            viewModel.uiState.value.activeDialog,
        )
    }

    @Test
    fun `onDeleteIconClick sets activeDialog with DELETE type`() = runTest {
        val book = Book(
            bookId = 1,
            title = "Cien años de soledad",
            author = "Author",
            genre = "Novel",
            releaseDate = "2020",
            numPages = 100,
            currentPage = 50,
        )
        val booksFlow = MutableStateFlow(listOf(book))
        every { getBooksUseCase() } returns booksFlow
        viewModel = BookCollectionViewModel(
            getBooksUseCase = getBooksUseCase,
            getUserDisplayNameUseCase = getUserDisplayNameUseCase,
            getTimeOfDayUseCase = getTimeOfDayUseCase,
            deleteBookUseCase = deleteBookUseCase,
        )
        advanceUntilIdle()

        viewModel.onDeleteIconClick(1)

        assertEquals(
            BookDialogState(bookId = 1, bookTitle = "Cien años de soledad", type = BookDialogType.DELETE),
            viewModel.uiState.value.activeDialog,
        )
    }

    @Test
    fun `dismissDialog resets activeDialog to null`() = runTest {
        val book = Book(
            bookId = 1,
            title = "Book",
            author = "Author",
            genre = "Novel",
            releaseDate = "2020",
            numPages = 100,
            currentPage = 50,
        )
        val booksFlow = MutableStateFlow(listOf(book))
        every { getBooksUseCase() } returns booksFlow
        viewModel = BookCollectionViewModel(
            getBooksUseCase = getBooksUseCase,
            getUserDisplayNameUseCase = getUserDisplayNameUseCase,
            getTimeOfDayUseCase = getTimeOfDayUseCase,
            deleteBookUseCase = deleteBookUseCase,
        )
        advanceUntilIdle()
        viewModel.onEditIconClick(1)

        viewModel.dismissDialog()

        assertEquals(null, viewModel.uiState.value.activeDialog)
    }

    @Test
    fun `confirmDelete dismisses dialog`() = runTest {
        val bookToDelete = Book(
            bookId = 1,
            title = "Book to delete",
            author = "Author",
            genre = "Novel",
            releaseDate = "2020",
            numPages = 100,
            currentPage = 50,
        )
        val booksFlow = MutableStateFlow(listOf(bookToDelete))
        every { getBooksUseCase() } returns booksFlow
        viewModel = BookCollectionViewModel(
            getBooksUseCase = getBooksUseCase,
            getUserDisplayNameUseCase = getUserDisplayNameUseCase,
            getTimeOfDayUseCase = getTimeOfDayUseCase,
            deleteBookUseCase = deleteBookUseCase,
        )
        advanceUntilIdle()
        viewModel.onDeleteIconClick(1)

        viewModel.confirmDelete()
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.activeDialog)
    }

    @Test
    fun `confirmDelete invokes deleteBookUseCase with selected book`() = runTest {
        val bookToDelete = Book(
            bookId = 1,
            title = "Book to delete",
            author = "Author",
            genre = "Novel",
            releaseDate = "2020",
            numPages = 100,
            currentPage = 50,
        )
        val booksFlow = MutableStateFlow(listOf(bookToDelete))
        every { getBooksUseCase() } returns booksFlow
        viewModel = BookCollectionViewModel(
            getBooksUseCase = getBooksUseCase,
            getUserDisplayNameUseCase = getUserDisplayNameUseCase,
            getTimeOfDayUseCase = getTimeOfDayUseCase,
            deleteBookUseCase = deleteBookUseCase,
        )
        advanceUntilIdle()

        viewModel.onDeleteIconClick(1)
        coEvery { deleteBookUseCase(bookToDelete) } returns Result.success(Unit)

        viewModel.confirmDelete()
        advanceUntilIdle()

        coVerify { deleteBookUseCase(bookToDelete) }
    }
}
