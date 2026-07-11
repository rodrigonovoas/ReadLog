package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignInWithGoogleUseCaseTest {

    private val repository = mockk<AuthRepository>()
    private val useCase = SignInWithGoogleUseCase(repository)

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        coEvery { repository.signInWithGoogle(any()) } returns Result.success(Unit)

        val result = useCase("fake_token")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val exception = RuntimeException("Auth failed")
        coEvery { repository.signInWithGoogle(any()) } returns Result.failure(exception)

        val result = useCase("fake_token")

        assertTrue(result.isFailure)
        assertEquals("Auth failed", result.exceptionOrNull()?.message)
    }
}
