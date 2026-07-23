package com.rodrigonovoa.readlog.ui.userprofile

import androidx.lifecycle.SavedStateHandle
import com.rodrigonovoa.readlog.domain.model.User
import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.usecase.GetCurrentUserUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetRemoteUserProfileInfoUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetUserDisplayNameUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetUserProfileInfoUseCase
import com.rodrigonovoa.readlog.domain.usecase.ToggleUserLikeUseCase
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var getUserDisplayNameUseCase: GetUserDisplayNameUseCase
    private lateinit var getUserProfileInfoUseCase: GetUserProfileInfoUseCase
    private lateinit var getRemoteUserProfileInfoUseCase: GetRemoteUserProfileInfoUseCase
    private lateinit var toggleUserLikeUseCase: ToggleUserLikeUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCurrentUserUseCase = mockk()
        getUserDisplayNameUseCase = mockk(relaxed = true)
        getUserProfileInfoUseCase = mockk()
        getRemoteUserProfileInfoUseCase = mockk()
        toggleUserLikeUseCase = mockk()
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
            toggleUserLikeUseCase = toggleUserLikeUseCase,
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
    fun `isOwnProfile is true when no userId nav arg is provided`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena Marín")
        every { getUserDisplayNameUseCase() } returns "Elena"
        coEvery { getUserProfileInfoUseCase("uid-1") } returns UserProfileInfo(userId = "uid-1")

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isOwnProfile)
    }

    @Test
    fun `uiState reflects cached stats returned by getUserProfileInfoUseCase`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena")
        coEvery { getUserProfileInfoUseCase("uid-1") } returns UserProfileInfo(
            userId = "uid-1",
            likesCount = 20,
            sessionsThisWeek = 4,
            weekTimeSeconds = 3900L,
            bookCollection = listOf("Book A", "Book B"),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

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
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena")
        coEvery { getUserProfileInfoUseCase("other-uid") } returns UserProfileInfo(
            userId = "other-uid",
            likesCount = 4,
        )
        coEvery { getUserProfileInfoUseCase("uid-1") } returns UserProfileInfo(userId = "uid-1")
        coEvery { getRemoteUserProfileInfoUseCase("other-uid") } returns Result.success(
            UserProfileInfo(
                userId = "other-uid",
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

        assertEquals(20, viewModel.uiState.value.likesCount)
        assertEquals(listOf(UserProfileBook(title = "Book A")), viewModel.uiState.value.collectionBooks)
        assertEquals("Elena", viewModel.uiState.value.userName)
        assertEquals("@elena_marin", viewModel.uiState.value.username)
        assertFalse(viewModel.uiState.value.isOwnProfile)
    }

    @Test
    fun `isLiked reflects the current user's followeds when viewing another profile`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena")
        coEvery { getUserProfileInfoUseCase("other-uid") } returns UserProfileInfo(userId = "other-uid")
        coEvery { getUserProfileInfoUseCase("uid-1") } returns UserProfileInfo(
            userId = "uid-1",
            followeds = listOf("other-uid"),
        )
        coEvery { getRemoteUserProfileInfoUseCase("other-uid") } returns Result.success(
            UserProfileInfo(userId = "other-uid")
        )

        val viewModel = createViewModel(userId = "other-uid")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLiked)
    }

    @Test
    fun `keeps cached identity when remote fetch fails for other user's profile`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena")
        coEvery { getUserProfileInfoUseCase("other-uid") } returns UserProfileInfo(
            userId = "other-uid",
            likesCount = 4,
        )
        coEvery { getUserProfileInfoUseCase("uid-1") } returns UserProfileInfo(userId = "uid-1")
        coEvery { getRemoteUserProfileInfoUseCase("other-uid") } returns Result.failure(RuntimeException("network error"))

        val viewModel = createViewModel(userId = "other-uid")
        advanceUntilIdle()

        assertEquals(4, viewModel.uiState.value.likesCount)
        assertEquals("", viewModel.uiState.value.userName)
        assertEquals("", viewModel.uiState.value.username)
    }

    @Test
    fun `onLikeClick calls ToggleUserLikeUseCase and flips isLiked and likesCount on success`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena")
        coEvery { getUserProfileInfoUseCase("other-uid") } returns UserProfileInfo(userId = "other-uid", likesCount = 4)
        coEvery { getUserProfileInfoUseCase("uid-1") } returns UserProfileInfo(userId = "uid-1")
        coEvery { getRemoteUserProfileInfoUseCase("other-uid") } returns Result.success(UserProfileInfo(userId = "other-uid", likesCount = 4))
        coEvery { toggleUserLikeUseCase("uid-1", "other-uid", true) } returns Result.success(Unit)

        val viewModel = createViewModel(userId = "other-uid")
        advanceUntilIdle()

        viewModel.onLikeClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLiked)
        assertEquals(5, viewModel.uiState.value.likesCount)
        coVerify { toggleUserLikeUseCase("uid-1", "other-uid", true) }
    }

    @Test
    fun `onLikeClick sets hasLikeError and leaves isLiked and likesCount unchanged on failure`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena")
        coEvery { getUserProfileInfoUseCase("other-uid") } returns UserProfileInfo(userId = "other-uid", likesCount = 4)
        coEvery { getUserProfileInfoUseCase("uid-1") } returns UserProfileInfo(userId = "uid-1")
        coEvery { getRemoteUserProfileInfoUseCase("other-uid") } returns Result.success(UserProfileInfo(userId = "other-uid", likesCount = 4))
        coEvery { toggleUserLikeUseCase("uid-1", "other-uid", true) } returns Result.failure(RuntimeException("No internet connection"))

        val viewModel = createViewModel(userId = "other-uid")
        advanceUntilIdle()

        viewModel.onLikeClick()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLiked)
        assertEquals(4, viewModel.uiState.value.likesCount)
        assertTrue(viewModel.uiState.value.hasLikeError)
    }

    @Test
    fun `onLikeClick is a no-op with no signed-in user`() = runTest {
        every { getCurrentUserUseCase() } returns null
        coEvery { getUserProfileInfoUseCase("other-uid") } returns UserProfileInfo(userId = "other-uid")
        coEvery { getRemoteUserProfileInfoUseCase("other-uid") } returns Result.success(UserProfileInfo(userId = "other-uid"))

        val viewModel = createViewModel(userId = "other-uid")
        advanceUntilIdle()

        viewModel.onLikeClick()
        advanceUntilIdle()

        coVerify(exactly = 0) { toggleUserLikeUseCase(any(), any(), any()) }
    }
}
