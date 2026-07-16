package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.Session
import com.rodrigonovoa.readlog.domain.repository.SessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddSessionUseCaseTest {

    private val repository = mockk<SessionRepository>()
    private val useCase = AddSessionUseCase(repository)

    @Test
    fun `invoke builds session from bookId and time and returns saved session`() = runTest {
        val savedSession = Session(sessionId = 1, bookId = 5, time = 120L)
        coEvery { repository.insertSession(any()) } returns savedSession

        val result = useCase(bookId = 5, time = 120L)

        coVerify {
            repository.insertSession(match { it.bookId == 5 && it.time == 120L && it.sessionId == 0 })
        }
        assertTrue(result.isSuccess)
        assertEquals(savedSession, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository throws`() = runTest {
        coEvery { repository.insertSession(any()) } throws RuntimeException("Insert error")

        val result = useCase(bookId = 5, time = 120L)

        assertTrue(result.isFailure)
        assertEquals("Insert error", result.exceptionOrNull()?.message)
    }
}
