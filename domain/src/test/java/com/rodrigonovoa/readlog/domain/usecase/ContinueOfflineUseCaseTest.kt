package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.fakes.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class ContinueOfflineUseCaseTest {

    private val repository = FakeAuthRepository()
    private val useCase = ContinueOfflineUseCase(repository)

    @Test
    fun `invoke returns success`() = runTest {
        val result = useCase()

        assertTrue(result.isSuccess)
    }
}
