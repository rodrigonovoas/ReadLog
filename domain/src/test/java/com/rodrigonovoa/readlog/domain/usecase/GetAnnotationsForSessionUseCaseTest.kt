package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.Annotation
import com.rodrigonovoa.readlog.domain.repository.AnnotationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAnnotationsForSessionUseCaseTest {

    private val repository = mockk<AnnotationRepository>()
    private val useCase = GetAnnotationsForSessionUseCase(repository)

    @Test
    fun `invoke returns annotations for the given session from repository`() = runTest {
        val expectedAnnotations = listOf(
            Annotation(annotationId = 1, sessionId = 7, annotation = "Great chapter"),
        )
        coEvery { repository.getAllAnnotationsListForSession(7) } returns expectedAnnotations

        val result = useCase(7)

        assertEquals(expectedAnnotations, result)
    }

    @Test
    fun `invoke returns empty list when session has no annotations`() = runTest {
        coEvery { repository.getAllAnnotationsListForSession(7) } returns emptyList()

        val result = useCase(7)

        assertEquals(emptyList<Annotation>(), result)
    }
}
