package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.Session
import com.rodrigonovoa.readlog.domain.repository.SessionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetSessionsForBookUseCaseTest {

    private val repository = mockk<SessionRepository>()
    private val useCase = GetSessionsForBookUseCase(repository)

    @Test
    fun `invoke returns sessions for the given book from repository`() = runTest {
        val expectedSessions = listOf(
            Session(sessionId = 1, bookId = 5, time = 120L),
            Session(sessionId = 2, bookId = 5, time = 300L),
        )
        every { repository.getSessionsForBook(5) } returns flowOf(expectedSessions)

        val result = useCase(5).first()

        assertEquals(expectedSessions, result)
    }

    @Test
    fun `invoke returns empty list when book has no sessions`() = runTest {
        every { repository.getSessionsForBook(5) } returns flowOf(emptyList())

        val result = useCase(5).first()

        assertEquals(emptyList<Session>(), result)
    }
}
