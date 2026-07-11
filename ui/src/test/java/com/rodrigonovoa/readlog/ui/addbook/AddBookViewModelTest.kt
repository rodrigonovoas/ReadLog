package com.rodrigonovoa.readlog.ui.addbook

import android.net.Uri
import com.rodrigonovoa.readlog.domain.usecase.AddBookUseCase
import com.rodrigonovoa.readlog.domain.usecase.CalculateReadingProgressUseCase
import com.rodrigonovoa.readlog.domain.usecase.CapCurrentPageUseCase
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
    private lateinit var viewModel: AddBookViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        addBookUseCase = mockk()
        validateFormUseCase = mockk(relaxed = true)
        capCurrentPageUseCase = mockk(relaxed = true)
        calculateProgressUseCase = mockk(relaxed = true)
        viewModel = AddBookViewModel(
            addBookUseCase = addBookUseCase,
            validateFormUseCase = validateFormUseCase,
            capCurrentPageUseCase = capCurrentPageUseCase,
            calculateProgressUseCase = calculateProgressUseCase,
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
        assertTrue(effect is AddBookEffect.NavigateBack)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `add book failure shows error`() = runTest {
        coEvery { addBookUseCase(any(), any(), any(), any()) } returns Result.failure(RuntimeException("Insert error"))

        viewModel.processIntent(AddBookIntent.OnTitleChanged("Title"))
        viewModel.processIntent(AddBookIntent.OnPagesChanged("100"))
        advanceUntilIdle()

        viewModel.processIntent(AddBookIntent.OnAddBookClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Insert error", viewModel.uiState.value.errorMessage)
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
    fun `back clicked emits navigate back`() = runTest {
        var effect: AddBookEffect? = null
        val collectJob = launch { effect = viewModel.effect.first() }

        viewModel.processIntent(AddBookIntent.OnBackClicked)
        advanceUntilIdle()
        collectJob.join()

        assertTrue(effect is AddBookEffect.NavigateBack)
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
}
