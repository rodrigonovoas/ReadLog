package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class ContinueOfflineUseCaseTest {

    private val repository = mockk<AuthRepository>()
    private val useCase = ContinueOfflineUseCase(repository)

    @Test
    fun `invoke returns success`() = runTest {
        coEvery { repository.continueOffline() } returns Result.success(Unit)

        val result = useCase()

        assertTrue(result.isSuccess)
    }
}
