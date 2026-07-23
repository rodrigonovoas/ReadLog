package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.repository.UserProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SetUsernameUseCaseTest {

    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var useCase: SetUsernameUseCase

    @Before
    fun setup() {
        userProfileRepository = mockk()
        useCase = SetUsernameUseCase(userProfileRepository)
    }

    @Test
    fun `trims the username before delegating to the repository`() = runTest {
        val expected = UserProfileInfo(userId = "uid", username = "elena_marin")
        coEvery { userProfileRepository.setUsername("uid", "elena_marin") } returns Result.success(expected)

        val result = useCase("uid", " elena_marin ")

        assertEquals(expected, result.getOrThrow())
        coVerify { userProfileRepository.setUsername("uid", "elena_marin") }
    }

    @Test
    fun `propagates failure from the repository`() = runTest {
        coEvery { userProfileRepository.setUsername("uid", "elena_marin") } returns Result.failure(RuntimeException("offline"))

        val result = useCase("uid", "elena_marin")

        assertEquals(true, result.isFailure)
    }
}
