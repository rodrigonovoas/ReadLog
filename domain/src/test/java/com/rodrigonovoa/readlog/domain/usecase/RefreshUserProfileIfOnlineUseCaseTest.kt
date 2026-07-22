package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.User
import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RefreshUserProfileIfOnlineUseCaseTest {

    private val getCurrentUserUseCase = mockk<GetCurrentUserUseCase>()
    private val isOnlineUseCase = mockk<IsOnlineUseCase>()
    private val refreshUserProfileInfoUseCase = mockk<RefreshUserProfileInfoUseCase>()
    private val useCase = RefreshUserProfileIfOnlineUseCase(
        getCurrentUserUseCase = getCurrentUserUseCase,
        isOnlineUseCase = isOnlineUseCase,
        refreshUserProfileInfoUseCase = refreshUserProfileInfoUseCase,
    )

    @Test
    fun `invoke refreshes profile when signed in and online`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena")
        every { isOnlineUseCase() } returns true
        coEvery { refreshUserProfileInfoUseCase("uid-1", "Elena") } returns Result.success(UserProfileInfo(userId = "uid-1"))

        useCase()

        coVerify { refreshUserProfileInfoUseCase("uid-1", "Elena") }
    }

    @Test
    fun `invoke does nothing when no user is signed in`() = runTest {
        every { getCurrentUserUseCase() } returns null
        every { isOnlineUseCase() } returns true

        useCase()

        coVerify(exactly = 0) { refreshUserProfileInfoUseCase(any(), any()) }
    }

    @Test
    fun `invoke does nothing when device is offline`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena")
        every { isOnlineUseCase() } returns false

        useCase()

        coVerify(exactly = 0) { refreshUserProfileInfoUseCase(any(), any()) }
    }

    @Test
    fun `invoke swallows failures from the refresh use case`() = runTest {
        every { getCurrentUserUseCase() } returns User("uid-1", "test@test.com", "Elena")
        every { isOnlineUseCase() } returns true
        coEvery { refreshUserProfileInfoUseCase("uid-1", "Elena") } returns Result.failure(RuntimeException("network error"))

        useCase()

        coVerify { refreshUserProfileInfoUseCase("uid-1", "Elena") }
    }
}
