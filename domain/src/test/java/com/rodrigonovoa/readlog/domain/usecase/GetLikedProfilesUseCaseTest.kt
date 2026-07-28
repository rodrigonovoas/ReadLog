package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.User
import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.repository.UserProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class GetLikedProfilesUseCaseTest {

    private val isOnlineUseCase: IsOnlineUseCase = mockk()
    private val getCurrentUserUseCase: GetCurrentUserUseCase = mockk()
    private val userProfileRepository: UserProfileRepository = mockk()
    private val useCase = GetLikedProfilesUseCase(
        isOnlineUseCase,
        getCurrentUserUseCase,
        userProfileRepository,
    )

    @Test
    fun `returns failure when offline`() = runTest {
        every { isOnlineUseCase() } returns false

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `returns empty list when no current user`() = runTest {
        every { isOnlineUseCase() } returns true
        every { getCurrentUserUseCase() } returns null

        val result = useCase()

        assertEquals(Result.success(emptyList<UserProfileInfo>()), result)
    }

    @Test
    fun `returns liked profiles from repository`() = runTest {
        every { isOnlineUseCase() } returns true
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Test User")
        val likedProfiles = listOf(
            UserProfileInfo(userId = "2", username = "elenalee"),
            UserProfileInfo(userId = "3", username = "elena_ruiz"),
        )
        coEvery { userProfileRepository.getLikedProfiles("uid-1") } returns Result.success(likedProfiles)

        val result = useCase()

        assertEquals(Result.success(likedProfiles), result)
        coVerify { userProfileRepository.getLikedProfiles("uid-1") }
    }

    @Test
    fun `returns failure when repository fails`() = runTest {
        every { isOnlineUseCase() } returns true
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Test User")
        val exception = RuntimeException("network error")
        coEvery { userProfileRepository.getLikedProfiles("uid-1") } returns Result.failure(exception)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
