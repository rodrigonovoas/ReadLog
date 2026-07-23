package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.repository.UserProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ToggleUserLikeUseCaseTest {

    private val isOnlineUseCase = mockk<IsOnlineUseCase>()
    private val userProfileRepository = mockk<UserProfileRepository>()
    private val useCase = ToggleUserLikeUseCase(
        isOnlineUseCase = isOnlineUseCase,
        userProfileRepository = userProfileRepository,
    )

    @Test
    fun `invoke delegates to repository when online`() = runTest {
        every { isOnlineUseCase() } returns true
        coEvery { userProfileRepository.setLiked("uid-1", "uid-2", true) } returns Result.success(Unit)

        val result = useCase("uid-1", "uid-2", true)

        assertEquals(true, result.isSuccess)
        coVerify { userProfileRepository.setLiked("uid-1", "uid-2", true) }
    }

    @Test
    fun `invoke returns failure without calling repository when offline`() = runTest {
        every { isOnlineUseCase() } returns false

        val result = useCase("uid-1", "uid-2", true)

        assertEquals(true, result.isFailure)
        coVerify(exactly = 0) { userProfileRepository.setLiked(any(), any(), any()) }
    }
}
