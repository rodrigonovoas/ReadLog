package com.rodrigonovoa.readlog.ui.booksession

import androidx.lifecycle.SavedStateHandle
import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.model.Session
import com.rodrigonovoa.readlog.domain.usecase.AddAnnotationUseCase
import com.rodrigonovoa.readlog.domain.usecase.AddSessionUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetBookByIdUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookSessionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getBookByIdUseCase: GetBookByIdUseCase
    private lateinit var addSessionUseCase: AddSessionUseCase
    private lateinit var addAnnotationUseCase: AddAnnotationUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: BookSessionViewModel

    private val bookId = 5

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getBookByIdUseCase = mockk(relaxed = true)
        addSessionUseCase = mockk()
        addAnnotationUseCase = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle(mapOf("bookId" to bookId))
        coEvery { addSessionUseCase(any(), any()) } returns Result.success(
            Session(sessionId = 1, bookId = bookId, time = 0L)
        )
        viewModel = createViewModel()
    }

    private fun createViewModel(): BookSessionViewModel {
        return BookSessionViewModel(
            getBookByIdUseCase = getBookByIdUseCase,
            addSessionUseCase = addSessionUseCase,
            addAnnotationUseCase = addAnnotationUseCase,
            savedStateHandle = savedStateHandle,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state starts at zero and stopped`() = runTest {
        val state = viewModel.uiState.value

        assertEquals(0L, state.elapsedSeconds)
        assertFalse(state.isRunning)
        assertFalse(state.showEndSessionDialog)
        assertEquals("", state.annotationText)
    }

    @Test
    fun `init loads book title from use case`() = runTest {
        val book = Book(
            bookId = bookId,
            title = "Cien años de soledad",
            author = "Gabriel García Márquez",
            genre = "Novel",
            releaseDate = "1967",
            numPages = 340,
            currentPage = 231,
        )
        coEvery { getBookByIdUseCase(bookId) } returns book
        val loadedViewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Cien años de soledad", loadedViewModel.uiState.value.bookTitle)
    }

    @Test
    fun `play starts the timer and increments every second`() = runTest {
        viewModel.processIntent(BookSessionIntent.OnPlayPauseClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isRunning)

        advanceTimeBy(3_000)
        advanceUntilIdle()

        assertEquals(3L, viewModel.uiState.value.elapsedSeconds)
    }

    @Test
    fun `pause stops the timer from incrementing`() = runTest {
        viewModel.processIntent(BookSessionIntent.OnPlayPauseClicked)
        advanceTimeBy(2_000)
        advanceUntilIdle()

        viewModel.processIntent(BookSessionIntent.OnPlayPauseClicked)
        advanceUntilIdle()

        val elapsedAfterPause = viewModel.uiState.value.elapsedSeconds
        assertFalse(viewModel.uiState.value.isRunning)

        advanceTimeBy(3_000)
        advanceUntilIdle()

        assertEquals(elapsedAfterPause, viewModel.uiState.value.elapsedSeconds)
    }

    @Test
    fun `stop pauses the timer and shows the end session dialog`() = runTest {
        viewModel.processIntent(BookSessionIntent.OnPlayPauseClicked)
        advanceTimeBy(2_000)
        advanceUntilIdle()

        viewModel.processIntent(BookSessionIntent.OnStopClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRunning)
        assertTrue(state.showEndSessionDialog)

        val elapsedAfterStop = state.elapsedSeconds
        advanceTimeBy(3_000)
        advanceUntilIdle()

        assertEquals(elapsedAfterStop, viewModel.uiState.value.elapsedSeconds)
    }

    @Test
    fun `dismissing the end session dialog keeps the timer paused`() = runTest {
        viewModel.processIntent(BookSessionIntent.OnPlayPauseClicked)
        advanceTimeBy(1_000)
        advanceUntilIdle()

        viewModel.processIntent(BookSessionIntent.OnStopClicked)
        advanceUntilIdle()
        val elapsedAfterStop = viewModel.uiState.value.elapsedSeconds

        viewModel.processIntent(BookSessionIntent.OnDismissEndSessionDialogClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showEndSessionDialog)
        assertFalse(state.isRunning)
        assertEquals(elapsedAfterStop, state.elapsedSeconds)
    }

    @Test
    fun `back without starting the timer emits NavigateBack directly without showing the dialog`() = runTest {
        var effect: BookSessionEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(BookSessionIntent.OnBackClicked)
        advanceUntilIdle()
        collectJob.join()

        assertEquals(BookSessionEffect.NavigateBack, effect)
        assertFalse(viewModel.uiState.value.showEndSessionDialog)
    }

    @Test
    fun `back with the timer running pauses it and shows the end session dialog`() = runTest {
        viewModel.processIntent(BookSessionIntent.OnPlayPauseClicked)
        advanceTimeBy(2_000)
        advanceUntilIdle()

        viewModel.processIntent(BookSessionIntent.OnBackClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRunning)
        assertTrue(state.showEndSessionDialog)
    }

    @Test
    fun `back after the timer was started and paused also shows the end session dialog`() = runTest {
        viewModel.processIntent(BookSessionIntent.OnPlayPauseClicked)
        advanceTimeBy(2_000)
        advanceUntilIdle()
        viewModel.processIntent(BookSessionIntent.OnPlayPauseClicked)
        advanceUntilIdle()

        viewModel.processIntent(BookSessionIntent.OnBackClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showEndSessionDialog)
    }

    @Test
    fun `back with annotation text but timer never started shows the end session dialog`() = runTest {
        viewModel.processIntent(BookSessionIntent.OnAnnotationTextChanged("Great chapter"))
        advanceUntilIdle()

        viewModel.processIntent(BookSessionIntent.OnBackClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showEndSessionDialog)
    }

    @Test
    fun `dismissing the dialog shown for annotations only does not start the timer`() = runTest {
        viewModel.processIntent(BookSessionIntent.OnAnnotationTextChanged("Great chapter"))
        advanceUntilIdle()

        viewModel.processIntent(BookSessionIntent.OnBackClicked)
        advanceUntilIdle()

        viewModel.processIntent(BookSessionIntent.OnDismissEndSessionDialogClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showEndSessionDialog)
        assertFalse(state.isRunning)
        assertEquals(0L, state.elapsedSeconds)
    }

    @Test
    fun `confirming the dialog after back discards the session and emits NavigateBack`() = runTest {
        viewModel.processIntent(BookSessionIntent.OnPlayPauseClicked)
        advanceTimeBy(4_000)
        advanceUntilIdle()

        viewModel.processIntent(BookSessionIntent.OnBackClicked)
        advanceUntilIdle()

        var effect: BookSessionEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(BookSessionIntent.OnConfirmEndSessionClicked)
        advanceUntilIdle()
        collectJob.join()

        coVerify(exactly = 0) { addSessionUseCase(any(), any()) }
        assertEquals(BookSessionEffect.NavigateBack, effect)
        assertFalse(viewModel.uiState.value.showEndSessionDialog)
    }

    @Test
    fun `dismissing the dialog after back resumes the timer from where it was left`() = runTest {
        viewModel.processIntent(BookSessionIntent.OnPlayPauseClicked)
        advanceTimeBy(2_000)
        advanceUntilIdle()

        viewModel.processIntent(BookSessionIntent.OnBackClicked)
        advanceUntilIdle()
        val elapsedBeforeResume = viewModel.uiState.value.elapsedSeconds

        viewModel.processIntent(BookSessionIntent.OnDismissEndSessionDialogClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showEndSessionDialog)
        assertTrue(state.isRunning)
        assertEquals(elapsedBeforeResume, state.elapsedSeconds)

        advanceTimeBy(2_000)
        advanceUntilIdle()
        assertEquals(elapsedBeforeResume + 2L, viewModel.uiState.value.elapsedSeconds)
    }

    @Test
    fun `annotation text change updates state`() = runTest {
        viewModel.processIntent(BookSessionIntent.OnAnnotationTextChanged("Great chapter"))
        advanceUntilIdle()

        assertEquals("Great chapter", viewModel.uiState.value.annotationText)
    }

    @Test
    fun `annotation text with up to 3 lines is accepted`() = runTest {
        val threeLineText = "Line one\nLine two\nLine three"

        viewModel.processIntent(BookSessionIntent.OnAnnotationTextChanged(threeLineText))
        advanceUntilIdle()

        assertEquals(threeLineText, viewModel.uiState.value.annotationText)
    }

    @Test
    fun `annotation text exceeding 3 lines is rejected`() = runTest {
        val threeLineText = "Line one\nLine two\nLine three"
        viewModel.processIntent(BookSessionIntent.OnAnnotationTextChanged(threeLineText))
        advanceUntilIdle()

        val fourLineText = "$threeLineText\nLine four"
        viewModel.processIntent(BookSessionIntent.OnAnnotationTextChanged(fourLineText))
        advanceUntilIdle()

        assertEquals(threeLineText, viewModel.uiState.value.annotationText)
    }

    @Test
    fun `confirming end session creates a session with bookId and elapsed time`() = runTest {
        viewModel.processIntent(BookSessionIntent.OnPlayPauseClicked)
        advanceTimeBy(4_000)
        advanceUntilIdle()

        viewModel.processIntent(BookSessionIntent.OnStopClicked)
        advanceUntilIdle()

        var effect: BookSessionEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(BookSessionIntent.OnConfirmEndSessionClicked)
        advanceUntilIdle()
        collectJob.join()

        coVerify { addSessionUseCase(bookId, 4L) }
        assertEquals(BookSessionEffect.NavigateBack, effect)
        assertFalse(viewModel.uiState.value.showEndSessionDialog)
    }

    @Test
    fun `confirming end session with annotation text saves the annotation`() = runTest {
        coEvery { addSessionUseCase(any(), any()) } returns Result.success(
            Session(sessionId = 42, bookId = bookId, time = 0L)
        )

        viewModel.processIntent(BookSessionIntent.OnAnnotationTextChanged("Great chapter"))
        viewModel.processIntent(BookSessionIntent.OnStopClicked)
        advanceUntilIdle()

        var effect: BookSessionEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(BookSessionIntent.OnConfirmEndSessionClicked)
        advanceUntilIdle()
        collectJob.join()

        coVerify { addAnnotationUseCase(42, "Great chapter") }
        assertEquals(BookSessionEffect.NavigateBack, effect)
    }

    @Test
    fun `confirming end session without annotation text does not save an annotation`() = runTest {
        viewModel.processIntent(BookSessionIntent.OnStopClicked)
        advanceUntilIdle()

        var effect: BookSessionEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(BookSessionIntent.OnConfirmEndSessionClicked)
        advanceUntilIdle()
        collectJob.join()

        coVerify(exactly = 0) { addAnnotationUseCase(any(), any()) }
        assertEquals(BookSessionEffect.NavigateBack, effect)
    }
}
