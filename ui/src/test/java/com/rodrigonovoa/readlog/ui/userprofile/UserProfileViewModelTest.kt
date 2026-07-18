package com.rodrigonovoa.readlog.ui.userprofile

import com.rodrigonovoa.readlog.domain.model.User
import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.usecase.GetCurrentUserUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetUserDisplayNameUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetUserProfileInfoUseCase
import com.rodrigonovoa.readlog.domain.usecase.RefreshUserProfileInfoUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class UserProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var getUserDisplayNameUseCase: GetUserDisplayNameUseCase
    private lateinit var getUserProfileInfoUseCase: GetUserProfileInfoUseCase
    private lateinit var refreshUserProfileInfoUseCase: RefreshUserProfileInfoUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCurrentUserUseCase = mockk()
        getUserDisplayNameUseCase = mockk(relaxed = true)
        getUserProfileInfoUseCase = mockk()
        refreshUserProfileInfoUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): UserProfileViewModel {
        return UserProfileViewModel(
            getCurrentUserUseCase = getCurrentUserUseCase,
            getUserDisplayNameUseCase = getUserDisplayNameUseCase,
            getUserProfileInfoUseCase = getUserProfileInfoUseCase,
            refreshUserProfileInfoUseCase = refreshUserProfileInfoUseCase,
        )
    }

    @Test
    fun `userName is filled with the value from getUserDisplayNameUseCase when a user is signed in`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena Marín")
        every { getUserDisplayNameUseCase() } returns "Elena"
        coEvery { getUserProfileInfoUseCase("uid-1") } returns UserProfileInfo(userId = "uid-1")
        coEvery { refreshUserProfileInfoUseCase("uid-1", "Elena Marín") } returns Result.success(UserProfileInfo(userId = "uid-1"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Elena", viewModel.uiState.value.userName)
        assertEquals("", viewModel.uiState.value.username)
    }

    @Test
    fun `userName falls back to reader and username stays empty when no user is signed in`() = runTest {
        every { getCurrentUserUseCase() } returns null
        every { getUserDisplayNameUseCase() } returns "reader"
        coEvery { getUserProfileInfoUseCase("") } returns UserProfileInfo(userId = "")
        coEvery { refreshUserProfileInfoUseCase("", null) } returns Result.success(UserProfileInfo(userId = ""))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("reader", viewModel.uiState.value.userName)
        assertEquals("", viewModel.uiState.value.username)
    }

    @Test
    fun `init paints cached local stats before the refresh completes`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena")
        coEvery { getUserProfileInfoUseCase("uid-1") } returns UserProfileInfo(
            userId = "uid-1",
            followersCount = 3,
            likesCount = 5,
            sessionsThisWeek = 2,
            weekTimeSeconds = 120L,
            bookCollection = listOf("Cached Book"),
        )
        coEvery { refreshUserProfileInfoUseCase("uid-1", "Elena") } returns Result.success(
            UserProfileInfo(userId = "uid-1")
        )

        val viewModel = createViewModel()

        assertEquals(3, viewModel.uiState.value.followersCount)
        assertEquals(5, viewModel.uiState.value.likesCount)
        assertEquals(2, viewModel.uiState.value.weeklySessionsCount)
        assertEquals(listOf(UserProfileBook(title = "Cached Book")), viewModel.uiState.value.collectionBooks)
    }

    @Test
    fun `uiState reflects refreshed stats once refresh succeeds`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena")
        coEvery { getUserProfileInfoUseCase("uid-1") } returns UserProfileInfo(userId = "uid-1")
        coEvery { refreshUserProfileInfoUseCase("uid-1", "Elena") } returns Result.success(
            UserProfileInfo(
                userId = "uid-1",
                followersCount = 10,
                likesCount = 20,
                sessionsThisWeek = 4,
                weekTimeSeconds = 3900L,
                bookCollection = listOf("Book A", "Book B"),
            )
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(10, viewModel.uiState.value.followersCount)
        assertEquals(20, viewModel.uiState.value.likesCount)
        assertEquals(4, viewModel.uiState.value.weeklySessionsCount)
        assertEquals("1h 05min", viewModel.uiState.value.weeklyTimeLabel)
        assertEquals(
            listOf(UserProfileBook(title = "Book A"), UserProfileBook(title = "Book B")),
            viewModel.uiState.value.collectionBooks,
        )
    }

    @Test
    fun `weeklyTimeLabel formats durations under an hour in minutes`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena")
        coEvery { getUserProfileInfoUseCase("uid-1") } returns UserProfileInfo(userId = "uid-1")
        coEvery { refreshUserProfileInfoUseCase("uid-1", "Elena") } returns Result.success(
            UserProfileInfo(userId = "uid-1", weekTimeSeconds = 1800L)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("30 min", viewModel.uiState.value.weeklyTimeLabel)
    }

    @Test
    fun `uiState keeps cached stats when refresh fails`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena")
        coEvery { getUserProfileInfoUseCase("uid-1") } returns UserProfileInfo(
            userId = "uid-1",
            followersCount = 7,
            likesCount = 9,
        )
        coEvery { refreshUserProfileInfoUseCase("uid-1", "Elena") } returns Result.failure(RuntimeException("network error"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(7, viewModel.uiState.value.followersCount)
        assertEquals(9, viewModel.uiState.value.likesCount)
    }
}
