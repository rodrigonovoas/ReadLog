package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.Annotation
import com.rodrigonovoa.readlog.domain.repository.AnnotationRepository
import javax.inject.Inject

class GetAnnotationsForSessionUseCase @Inject constructor(
    private val annotationRepository: AnnotationRepository
) {
    suspend operator fun invoke(sessionId: Int): List<Annotation> {
        return annotationRepository.getAllAnnotationsListForSession(sessionId)
    }
}
