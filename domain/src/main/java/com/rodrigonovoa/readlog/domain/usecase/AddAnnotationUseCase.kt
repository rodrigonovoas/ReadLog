package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.Annotation
import com.rodrigonovoa.readlog.domain.repository.AnnotationRepository
import javax.inject.Inject

class AddAnnotationUseCase @Inject constructor(
    private val annotationRepository: AnnotationRepository
) {
    suspend operator fun invoke(sessionId: Int, text: String): Result<Unit> {
        val annotation = Annotation(sessionId = sessionId, annotation = text.trim())
        return runCatching { annotationRepository.insertAnnotation(annotation) }
    }
}
