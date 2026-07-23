package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.repository.UserSearchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IsUsernameAvailableUseCaseTest {

    private lateinit var userSearchRepository: UserSearchRepository
    private lateinit var useCase: IsUsernameAvailableUseCase

    @Before
    fun setup() {
        userSearchRepository = mockk()
        useCase = IsUsernameAvailableUseCase(userSearchRepository)
    }

    @Test
    fun `returns true when username is not taken`() = runTest {
        coEvery { userSearchRepository.isUsernameTaken("elena_marin") } returns Result.success(false)

        val result = useCase("Elena_Marin")

        assertEquals(true, result.getOrThrow())
    }

    @Test
    fun `returns false when username is taken`() = runTest {
        coEvery { userSearchRepository.isUsernameTaken("elena_marin") } returns Result.success(true)

        val result = useCase(" elena_marin ")

        assertEquals(false, result.getOrThrow())
    }

    @Test
    fun `propagates failure from the repository`() = runTest {
        coEvery { userSearchRepository.isUsernameTaken("elena_marin") } returns Result.failure(RuntimeException("offline"))

        val result = useCase("elena_marin")

        assertTrue(result.isFailure)
    }
}
