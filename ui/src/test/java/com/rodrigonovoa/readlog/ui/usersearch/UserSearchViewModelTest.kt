package com.rodrigonovoa.readlog.ui.usersearch

import androidx.lifecycle.SavedStateHandle
import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.model.UserSearchResult
import com.rodrigonovoa.readlog.domain.usecase.GetLikedProfilesUseCase
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
    private lateinit var getLikedProfilesUseCase: GetLikedProfilesUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchUsersUseCase = mockk()
        getLikedProfilesUseCase = mockk()
        coEvery { searchUsersUseCase("") } returns Result.success(emptyList())
        coEvery { getLikedProfilesUseCase() } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        mode: UserSearchMode = UserSearchMode.SEARCH,
    ): UserSearchViewModel = UserSearchViewModel(
        SavedStateHandle(mapOf(UserSearchViewModel.MODE_ARG to mode.name)),
        searchUsersUseCase,
        getLikedProfilesUseCase,
    )

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

    @Test
    fun `loads liked profiles on init in likes mode`() = runTest {
        coEvery { getLikedProfilesUseCase() } returns Result.success(
            listOf(
                UserProfileInfo(userId = "1", username = "elenalee"),
                UserProfileInfo(userId = "2", username = "elena_ruiz"),
            )
        )

        val viewModel = createViewModel(mode = UserSearchMode.LIKES)
        advanceUntilIdle()

        assertEquals(
            listOf(
                UserSearchResultUi(userId = "1", username = "elenalee"),
                UserSearchResultUi(userId = "2", username = "elena_ruiz"),
            ),
            viewModel.uiState.value.results,
        )
        assertEquals(UserSearchMode.LIKES, viewModel.uiState.value.mode)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(false, viewModel.uiState.value.hasError)
    }

    @Test
    fun `ignores query changes in likes mode`() = runTest {
        coEvery { getLikedProfilesUseCase() } returns Result.success(emptyList())

        val viewModel = createViewModel(mode = UserSearchMode.LIKES)
        viewModel.onQueryChange("elen")
        advanceUntilIdle()

        coVerify(exactly = 0) { searchUsersUseCase(any()) }
        assertEquals("", viewModel.uiState.value.query)
    }

    @Test
    fun `sets error state when liked profiles fail to load`() = runTest {
        coEvery { getLikedProfilesUseCase() } returns Result.failure(RuntimeException("offline"))

        val viewModel = createViewModel(mode = UserSearchMode.LIKES)
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.hasError)
        assertEquals(emptyList<UserSearchResultUi>(), viewModel.uiState.value.results)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }
}
