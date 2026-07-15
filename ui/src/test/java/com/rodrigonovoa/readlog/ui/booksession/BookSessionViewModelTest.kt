package com.rodrigonovoa.readlog.ui.booksession

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
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
    private lateinit var viewModel: BookSessionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = BookSessionViewModel()
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
    fun `confirming end session closes dialog and emits navigate back`() = runTest {
        viewModel.processIntent(BookSessionIntent.OnStopClicked)
        advanceUntilIdle()

        var effect: BookSessionEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(BookSessionIntent.OnConfirmEndSessionClicked)
        advanceUntilIdle()
        collectJob.join()

        assertEquals(BookSessionEffect.NavigateBack, effect)
        assertFalse(viewModel.uiState.value.showEndSessionDialog)
    }
}
