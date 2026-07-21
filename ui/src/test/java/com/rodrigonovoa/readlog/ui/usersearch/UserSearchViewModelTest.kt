package com.rodrigonovoa.readlog.ui.usersearch

import com.rodrigonovoa.readlog.domain.model.UserSearchResult
import com.rodrigonovoa.readlog.domain.usecase.SearchUsersUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserSearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var searchUsersUseCase: SearchUsersUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchUsersUseCase = mockk()
        coEvery { searchUsersUseCase("") } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): UserSearchViewModel = UserSearchViewModel(searchUsersUseCase)

    @Test
    fun `does not search before the debounce window elapses`() = runTest {
        coEvery { searchUsersUseCase("elen") } returns Result.success(
            listOf(UserSearchResult(userId = "1", username = "elenalee"))
        )

        val viewModel = createViewModel()
        viewModel.onQueryChange("elen")
        advanceTimeBy(100)

        coVerify(exactly = 0) { searchUsersUseCase("elen") }
    }

    @Test
    fun `emits results once the debounce window elapses`() = runTest {
        coEvery { searchUsersUseCase("elen") } returns Result.success(
            listOf(UserSearchResult(userId = "1", username = "elenalee"))
        )

        val viewModel = createViewModel()
        viewModel.onQueryChange("elen")
        advanceUntilIdle()

        assertEquals(
            listOf(UserSearchResultUi(userId = "1", username = "elenalee")),
            viewModel.uiState.value.results,
        )
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(false, viewModel.uiState.value.hasError)
    }

    @Test
    fun `only the latest query is searched when the user types quickly`() = runTest {
        coEvery { searchUsersUseCase("elena") } returns Result.success(
            listOf(UserSearchResult(userId = "1", username = "elenalee"))
        )

        val viewModel = createViewModel()
        viewModel.onQueryChange("elen")
        viewModel.onQueryChange("elena")
        advanceUntilIdle()

        coVerify(exactly = 0) { searchUsersUseCase("elen") }
        assertEquals(
            listOf(UserSearchResultUi(userId = "1", username = "elenalee")),
            viewModel.uiState.value.results,
        )
    }

    @Test
    fun `sets error state and clears results when search fails`() = runTest {
        coEvery { searchUsersUseCase("elen") } returns Result.failure(RuntimeException("offline"))

        val viewModel = createViewModel()
        viewModel.onQueryChange("elen")
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.hasError)
        assertEquals(emptyList<UserSearchResultUi>(), viewModel.uiState.value.results)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }
}
