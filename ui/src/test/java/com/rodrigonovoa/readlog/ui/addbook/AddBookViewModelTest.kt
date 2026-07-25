package com.rodrigonovoa.readlog.ui.addbook

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.usecase.AddBookUseCase
import com.rodrigonovoa.readlog.domain.usecase.CalculateReadingProgressUseCase
import com.rodrigonovoa.readlog.domain.usecase.CapCurrentPageUseCase
import com.rodrigonovoa.readlog.domain.usecase.FetchBookByIsbnUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetBookByIdUseCase
import com.rodrigonovoa.readlog.domain.usecase.RefreshUserProfileIfOnlineUseCase
import com.rodrigonovoa.readlog.domain.usecase.UpdateBookUseCase
import com.rodrigonovoa.readlog.domain.usecase.ValidateAddBookFormUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddBookViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var addBookUseCase: AddBookUseCase
    private lateinit var validateFormUseCase: ValidateAddBookFormUseCase
    private lateinit var capCurrentPageUseCase: CapCurrentPageUseCase
    private lateinit var calculateProgressUseCase: CalculateReadingProgressUseCase
    private lateinit var getBookByIdUseCase: GetBookByIdUseCase
    private lateinit var updateBookUseCase: UpdateBookUseCase
    private lateinit var refreshUserProfileIfOnlineUseCase: RefreshUserProfileIfOnlineUseCase
    private lateinit var fetchBookByIsbnUseCase: FetchBookByIsbnUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: AddBookViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        addBookUseCase = mockk()
        validateFormUseCase = mockk(relaxed = true)
        capCurrentPageUseCase = mockk(relaxed = true)
        calculateProgressUseCase = mockk(relaxed = true)
        getBookByIdUseCase = mockk(relaxed = true)
        updateBookUseCase = mockk(relaxed = true)
        refreshUserProfileIfOnlineUseCase = mockk(relaxed = true)
        fetchBookByIsbnUseCase = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle()
        viewModel = createViewModel()
    }

    private fun createViewModel(): AddBookViewModel {
        return AddBookViewModel(
            addBookUseCase = addBookUseCase,
            validateFormUseCase = validateFormUseCase,
            capCurrentPageUseCase = capCurrentPageUseCase,
            calculateProgressUseCase = calculateProgressUseCase,
            getBookByIdUseCase = getBookByIdUseCase,
            updateBookUseCase = updateBookUseCase,
            refreshUserProfileIfOnlineUseCase = refreshUserProfileIfOnlineUseCase,
            fetchBookByIsbnUseCase = fetchBookByIsbnUseCase,
            savedStateHandle = savedStateHandle,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty fields and disabled submit`() = runTest {
        val state = viewModel.uiState.value

        assertEquals("", state.title)
        assertEquals("", state.author)
        assertEquals("", state.pages)
        assertEquals("", state.currentPage)
        assertNull(state.coverUri)
        assertFalse(state.isSubmitEnabled)
        assertEquals(0, state.progressPercentage)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(AddBookMode.Manual, state.selectedMode)
    }

    @Test
    fun `title change updates state`() = runTest {
        every { validateFormUseCase(any(), any(), any()) } returns false

        viewModel.processIntent(AddBookIntent.OnTitleChanged("Test Title"))
        advanceUntilIdle()

        assertEquals("Test Title", viewModel.uiState.value.title)
    }

    @Test
    fun `pages change updates state with use case delegation`() = runTest {
        every { capCurrentPageUseCase(any(), any()) } returns "231"
        every { calculateProgressUseCase(any(), any()) } returns 67
        every { validateFormUseCase(any(), any(), any()) } returns true

        viewModel.processIntent(AddBookIntent.OnPagesChanged("340"))
        advanceUntilIdle()

        assertEquals("340", viewModel.uiState.value.pages)
        assertEquals("231", viewModel.uiState.value.currentPage)
        assertEquals(67, viewModel.uiState.value.progressPercentage)
        assertTrue(viewModel.uiState.value.isSubmitEnabled)
    }

    @Test
    fun `current page change updates state with use case delegation`() = runTest {
        every { capCurrentPageUseCase(any(), any()) } returns "50"
        every { calculateProgressUseCase(any(), any()) } returns 50
        every { validateFormUseCase(any(), any(), any()) } returns true

        viewModel.processIntent(AddBookIntent.OnCurrentPageChanged("50"))
        advanceUntilIdle()

        assertEquals("50", viewModel.uiState.value.currentPage)
        assertEquals(50, viewModel.uiState.value.progressPercentage)
    }

    @Test
    fun `submit is enabled with valid title and pages and empty currentPage`() = runTest {
        every { capCurrentPageUseCase(any(), any()) } returns ""
        every { calculateProgressUseCase(any(), any()) } returns 0
        every { validateFormUseCase(any(), any(), any()) } returns true

        viewModel.processIntent(AddBookIntent.OnTitleChanged("Title"))
        viewModel.processIntent(AddBookIntent.OnPagesChanged("100"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSubmitEnabled)
    }

    @Test
    fun `submit is disabled when form is invalid`() = runTest {
        every { validateFormUseCase(any(), any(), any()) } returns false

        viewModel.processIntent(AddBookIntent.OnTitleChanged(""))
        viewModel.processIntent(AddBookIntent.OnPagesChanged("100"))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSubmitEnabled)
    }

    @Test
    fun `add book success with empty currentPage passes zero`() = runTest {
        coEvery { addBookUseCase(any(), any(), any(), any()) } returns Result.success(Unit)

        viewModel.processIntent(AddBookIntent.OnTitleChanged("Title"))
        viewModel.processIntent(AddBookIntent.OnAuthorChanged("Author"))
        viewModel.processIntent(AddBookIntent.OnPagesChanged("100"))
        advanceUntilIdle()

        var effect: AddBookEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(AddBookIntent.OnAddBookClicked)
        advanceUntilIdle()
        collectJob.join()

        coVerify { addBookUseCase("Title", "Author", 100, 0) }
        coVerify { refreshUserProfileIfOnlineUseCase() }
        assertTrue(effect is AddBookEffect.NavigateBack)
    }

    @Test
    fun `add book success emits navigate back and passes currentPage`() = runTest {
        coEvery { addBookUseCase(any(), any(), any(), any()) } returns Result.success(Unit)
        every { capCurrentPageUseCase(any(), any()) } returns "50"
        every { calculateProgressUseCase(any(), any()) } returns 50
        every { validateFormUseCase(any(), any(), any()) } returns true

        viewModel.processIntent(AddBookIntent.OnTitleChanged("Title"))
        viewModel.processIntent(AddBookIntent.OnAuthorChanged("Author"))
        viewModel.processIntent(AddBookIntent.OnPagesChanged("100"))
        viewModel.processIntent(AddBookIntent.OnCurrentPageChanged("50"))
        advanceUntilIdle()

        var effect: AddBookEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(AddBookIntent.OnAddBookClicked)
        advanceUntilIdle()
        collectJob.join()

        coVerify { addBookUseCase("Title", "Author", 100, 50) }
        coVerify { refreshUserProfileIfOnlineUseCase() }
        assertTrue(effect is AddBookEffect.NavigateBack)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `add book failure shows error and does not refresh profile`() = runTest {
        coEvery { addBookUseCase(any(), any(), any(), any()) } returns Result.failure(RuntimeException("Insert error"))

        viewModel.processIntent(AddBookIntent.OnTitleChanged("Title"))
        viewModel.processIntent(AddBookIntent.OnPagesChanged("100"))
        advanceUntilIdle()

        viewModel.processIntent(AddBookIntent.OnAddBookClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Insert error", viewModel.uiState.value.errorMessage)
        coVerify(exactly = 0) { refreshUserProfileIfOnlineUseCase() }
    }

    @Test
    fun `cover selected updates uri`() = runTest {
        val mockUri = mockk<Uri>()

        viewModel.processIntent(AddBookIntent.OnCoverSelected(mockUri))
        advanceUntilIdle()

        assertEquals(mockUri, viewModel.uiState.value.coverUri)
    }

    @Test
    fun `mode selection updates state`() = runTest {
        viewModel.processIntent(AddBookIntent.OnModeSelected(AddBookMode.Scan))
        advanceUntilIdle()

        assertEquals(AddBookMode.Scan, viewModel.uiState.value.selectedMode)
    }

    @Test
    fun `dismiss error clears message`() = runTest {
        coEvery { addBookUseCase(any(), any(), any(), any()) } returns Result.failure(RuntimeException("Error"))

        viewModel.processIntent(AddBookIntent.OnTitleChanged("Title"))
        viewModel.processIntent(AddBookIntent.OnPagesChanged("100"))
        advanceUntilIdle()
        viewModel.processIntent(AddBookIntent.OnAddBookClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.errorMessage != null)

        viewModel.processIntent(AddBookIntent.DismissError)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `back clicked with empty state emits navigate back`() = runTest {
        var effect: AddBookEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(AddBookIntent.OnBackClicked)
        advanceUntilIdle()
        collectJob.join()

        assertTrue(effect is AddBookEffect.NavigateBack)
    }

    @Test
    fun `back clicked with data shows exit confirmation`() = runTest {
        viewModel.processIntent(AddBookIntent.OnTitleChanged("Test"))
        advanceUntilIdle()

        val effects = mutableListOf<AddBookEffect>()
        val collectJob = launch {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.processIntent(AddBookIntent.OnBackClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showExitConfirmation)
        assertTrue(effects.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun `confirm exit emits navigate back and hides dialog`() = runTest {
        viewModel.processIntent(AddBookIntent.OnTitleChanged("Test"))
        viewModel.processIntent(AddBookIntent.OnBackClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showExitConfirmation)

        var effect: AddBookEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(AddBookIntent.OnConfirmExitClicked)
        advanceUntilIdle()
        collectJob.join()

        assertTrue(effect is AddBookEffect.NavigateBack)
        assertFalse(viewModel.uiState.value.showExitConfirmation)
    }

    @Test
    fun `dismiss exit hides dialog`() = runTest {
        viewModel.processIntent(AddBookIntent.OnTitleChanged("Test"))
        viewModel.processIntent(AddBookIntent.OnBackClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showExitConfirmation)

        viewModel.processIntent(AddBookIntent.OnDismissExitClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showExitConfirmation)
    }

    @Test
    fun `launch cover picker emits request effect`() = runTest {
        var effect: AddBookEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(AddBookIntent.LaunchCoverPicker)
        advanceUntilIdle()
        collectJob.join()

        assertTrue(effect is AddBookEffect.RequestCoverPicker)
    }

    @Test
    fun `edit mode pre-fills state from loaded book`() = runTest {
        val book = Book(
            bookId = 1,
            title = "Existing Title",
            author = "Existing Author",
            genre = "Novel",
            releaseDate = "2020",
            numPages = 300,
            currentPage = 150,
        )
        coEvery { getBookByIdUseCase(1) } returns book
        every { validateFormUseCase(any(), any(), any()) } returns true
        savedStateHandle["bookId"] = 1
        val editViewModel = createViewModel()
        advanceUntilIdle()

        val state = editViewModel.uiState.value
        assertEquals("Existing Title", state.title)
        assertEquals("Existing Author", state.author)
        assertEquals("300", state.pages)
        assertEquals("150", state.currentPage)
        assertTrue(state.isSubmitEnabled)
        assertTrue(state.isEditMode)
        assertEquals(1, state.bookId)
    }

    @Test
    fun `edit mode back click emits navigate back without confirmation`() = runTest {
        val book = Book(
            bookId = 1,
            title = "Existing Title",
            author = "Existing Author",
            genre = "Novel",
            releaseDate = "2020",
            numPages = 300,
            currentPage = 150,
        )
        coEvery { getBookByIdUseCase(1) } returns book
        savedStateHandle["bookId"] = 1
        val editViewModel = createViewModel()
        advanceUntilIdle()

        var effect: AddBookEffect? = null
        val collectJob = launch { effect = editViewModel.effect.first() }

        editViewModel.processIntent(AddBookIntent.OnBackClicked)
        advanceUntilIdle()
        collectJob.join()

        assertTrue(effect is AddBookEffect.NavigateBack)
        assertFalse(editViewModel.uiState.value.showExitConfirmation)
    }

    @Test
    fun `edit mode submit calls updateBookUseCase and navigates back`() = runTest {
        val book = Book(
            bookId = 1,
            title = "Existing Title",
            author = "Existing Author",
            genre = "Novel",
            releaseDate = "2020",
            numPages = 300,
            currentPage = 150,
        )
        coEvery { getBookByIdUseCase(1) } returns book
        savedStateHandle["bookId"] = 1
        val editViewModel = createViewModel()
        advanceUntilIdle()

        coEvery { updateBookUseCase(any(), any(), any(), any(), any()) } returns Result.success(Unit)

        var effect: AddBookEffect? = null
        val collectJob = launch { effect = editViewModel.effect.first() }

        editViewModel.processIntent(AddBookIntent.OnAddBookClicked)
        advanceUntilIdle()
        collectJob.join()

        coVerify {
            updateBookUseCase(
                book,
                "Existing Title",
                "Existing Author",
                300,
                150,
            )
        }
        coVerify { refreshUserProfileIfOnlineUseCase() }
        assertTrue(effect is AddBookEffect.NavigateBack)
    }

    @Test
    fun `camera permission result granted updates state`() = runTest {
        viewModel.processIntent(AddBookIntent.OnCameraPermissionResult(true))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasCameraPermission)
    }

    @Test
    fun `camera permission result denied in scan mode emits request permission effect`() = runTest {
        viewModel.processIntent(AddBookIntent.OnModeSelected(AddBookMode.Scan))
        advanceUntilIdle()

        var effect: AddBookEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(AddBookIntent.OnCameraPermissionResult(false))
        advanceUntilIdle()
        collectJob.join()

        assertFalse(viewModel.uiState.value.hasCameraPermission)
        assertTrue(effect is AddBookEffect.RequestCameraPermission)
    }

    @Test
    fun `barcode scanned success fills form and switches to manual tab`() = runTest {
        val metadata = com.rodrigonovoa.readlog.domain.model.BookMetadata(
            title = "Scanned Title",
            author = "Scanned Author",
            genre = "Fiction",
            releaseDate = "2020",
            numPages = 250,
        )
        coEvery { fetchBookByIsbnUseCase("9781234567890") } returns Result.success(metadata)
        every { capCurrentPageUseCase(any(), any()) } returns ""
        every { calculateProgressUseCase(any(), any()) } returns 0
        every { validateFormUseCase(any(), any(), any()) } returns true

        viewModel.processIntent(AddBookIntent.OnBarcodeScanned("9781234567890"))
        advanceUntilIdle()

        coVerify { fetchBookByIsbnUseCase("9781234567890") }
        assertEquals(AddBookMode.Manual, viewModel.uiState.value.selectedMode)
        assertEquals("Scanned Title", viewModel.uiState.value.title)
        assertEquals("Scanned Author", viewModel.uiState.value.author)
        assertEquals("250", viewModel.uiState.value.pages)
        assertFalse(viewModel.uiState.value.isScanning)
        assertNull(viewModel.uiState.value.scanError)
    }

    @Test
    fun `barcode scanned network error shows scan error`() = runTest {
        coEvery { fetchBookByIsbnUseCase("9781234567890") } returns Result.failure(
            java.net.UnknownHostException()
        )

        viewModel.processIntent(AddBookIntent.OnBarcodeScanned("9781234567890"))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isScanning)
        assertEquals(ScanError.Network, viewModel.uiState.value.scanError)
    }

    @Test
    fun `barcode scanned not found error shows scan error`() = runTest {
        coEvery { fetchBookByIsbnUseCase("9781234567890") } returns Result.failure(
            NoSuchElementException()
        )

        viewModel.processIntent(AddBookIntent.OnBarcodeScanned("9781234567890"))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isScanning)
        assertEquals(ScanError.NotFound, viewModel.uiState.value.scanError)
    }

    @Test
    fun `scan retry clicked clears scan error and requests permission`() = runTest {
        coEvery { fetchBookByIsbnUseCase("9781234567890") } returns Result.failure(
            NoSuchElementException()
        )
        viewModel.processIntent(AddBookIntent.OnBarcodeScanned("9781234567890"))
        advanceUntilIdle()
        assertEquals(ScanError.NotFound, viewModel.uiState.value.scanError)

        var effect: AddBookEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(AddBookIntent.OnScanRetryClicked)
        advanceUntilIdle()
        collectJob.join()

        assertNull(viewModel.uiState.value.scanError)
        assertTrue(effect is AddBookEffect.RequestCameraPermission)
    }

    @Test
    fun `scan error dismissed clears scan error`() = runTest {
        coEvery { fetchBookByIsbnUseCase("9781234567890") } returns Result.failure(
            RuntimeException()
        )
        viewModel.processIntent(AddBookIntent.OnBarcodeScanned("9781234567890"))
        advanceUntilIdle()
        assertEquals(ScanError.Unknown, viewModel.uiState.value.scanError)

        viewModel.processIntent(AddBookIntent.OnScanErrorDismissed)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.scanError)
    }
}
