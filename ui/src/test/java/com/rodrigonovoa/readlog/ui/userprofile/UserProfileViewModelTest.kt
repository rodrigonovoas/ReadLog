package com.rodrigonovoa.readlog.ui.userprofile

import androidx.lifecycle.SavedStateHandle
import com.rodrigonovoa.readlog.domain.model.User
import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.usecase.GetCurrentUserUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetRemoteUserProfileInfoUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetUserDisplayNameUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetUserProfileInfoUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
    private lateinit var getRemoteUserProfileInfoUseCase: GetRemoteUserProfileInfoUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCurrentUserUseCase = mockk()
        getUserDisplayNameUseCase = mockk(relaxed = true)
        getUserProfileInfoUseCase = mockk()
        getRemoteUserProfileInfoUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(userId: String? = null): UserProfileViewModel {
        val savedStateHandle = if (userId != null) {
            SavedStateHandle(mapOf("userId" to userId))
        } else {
            SavedStateHandle()
        }
        return UserProfileViewModel(
            savedStateHandle = savedStateHandle,
            getCurrentUserUseCase = getCurrentUserUseCase,
            getUserDisplayNameUseCase = getUserDisplayNameUseCase,
            getUserProfileInfoUseCase = getUserProfileInfoUseCase,
            getRemoteUserProfileInfoUseCase = getRemoteUserProfileInfoUseCase,
        )
    }

    @Test
    fun `userName is filled with the value from getUserDisplayNameUseCase when a user is signed in`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena Marín")
        every { getUserDisplayNameUseCase() } returns "Elena"
        coEvery { getUserProfileInfoUseCase("uid-1") } returns UserProfileInfo(userId = "uid-1")

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

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("reader", viewModel.uiState.value.userName)
        assertEquals("", viewModel.uiState.value.username)
    }

    @Test
    fun `own profile shows the username once it has been set`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena Marín")
        every { getUserDisplayNameUseCase() } returns "Elena"
        coEvery { getUserProfileInfoUseCase("uid-1") } returns UserProfileInfo(
            userId = "uid-1", username = "elena_marin"
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("@elena_marin", viewModel.uiState.value.username)
    }

    @Test
    fun `uiState reflects cached stats returned by getUserProfileInfoUseCase`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena")
        coEvery { getUserProfileInfoUseCase("uid-1") } returns UserProfileInfo(
            userId = "uid-1",
            followersCount = 10,
            likesCount = 20,
            sessionsThisWeek = 4,
            weekTimeSeconds = 3900L,
            bookCollection = listOf("Book A", "Book B"),
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
        coEvery { getUserProfileInfoUseCase("uid-1") } returns UserProfileInfo(
            userId = "uid-1", weekTimeSeconds = 1800L
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("30 min", viewModel.uiState.value.weeklyTimeLabel)
    }

    @Test
    fun `loads other user's profile from cache then remote when userId is provided`() = runTest {
        coEvery { getUserProfileInfoUseCase("other-uid") } returns UserProfileInfo(
            userId = "other-uid",
            followersCount = 3,
            likesCount = 4,
        )
        coEvery { getRemoteUserProfileInfoUseCase("other-uid") } returns Result.success(
            UserProfileInfo(
                userId = "other-uid",
                followersCount = 10,
                likesCount = 20,
                sessionsThisWeek = 2,
                weekTimeSeconds = 120L,
                bookCollection = listOf("Book A"),
                displayName = "Elena Marín",
                username = "elena_marin",
            )
        )

        val viewModel = createViewModel(userId = "other-uid")
        advanceUntilIdle()

        assertEquals(10, viewModel.uiState.value.followersCount)
        assertEquals(20, viewModel.uiState.value.likesCount)
        assertEquals(listOf(UserProfileBook(title = "Book A")), viewModel.uiState.value.collectionBooks)
        assertEquals("Elena", viewModel.uiState.value.userName)
        assertEquals("@elena_marin", viewModel.uiState.value.username)
        verify(exactly = 0) { getCurrentUserUseCase() }
    }

    @Test
    fun `keeps cached identity when remote fetch fails for other user's profile`() = runTest {
        coEvery { getUserProfileInfoUseCase("other-uid") } returns UserProfileInfo(
            userId = "other-uid",
            followersCount = 3,
            likesCount = 4,
        )
        coEvery { getRemoteUserProfileInfoUseCase("other-uid") } returns Result.failure(RuntimeException("network error"))

        val viewModel = createViewModel(userId = "other-uid")
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.followersCount)
        assertEquals(4, viewModel.uiState.value.likesCount)
        assertEquals("", viewModel.uiState.value.userName)
        assertEquals("", viewModel.uiState.value.username)
    }
}
