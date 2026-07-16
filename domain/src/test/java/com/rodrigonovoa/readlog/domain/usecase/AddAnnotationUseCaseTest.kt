package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.repository.AnnotationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddAnnotationUseCaseTest {

    private val repository = mockk<AnnotationRepository>()
    private val useCase = AddAnnotationUseCase(repository)

    @Test
    fun `invoke builds annotation from sessionId and trimmed text`() = runTest {
        coEvery { repository.insertAnnotation(any()) } returns Unit

        val result = useCase(sessionId = 3, text = "  Great chapter  ")

        coVerify {
            repository.insertAnnotation(
                match { it.sessionId == 3 && it.annotation == "Great chapter" && it.annotationId == 0 }
            )
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure when repository throws`() = runTest {
        coEvery { repository.insertAnnotation(any()) } throws RuntimeException("Insert error")

        val result = useCase(sessionId = 3, text = "Note")

        assertTrue(result.isFailure)
        assertEquals("Insert error", result.exceptionOrNull()?.message)
    }
}
