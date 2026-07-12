package com.rodrigonovoa.readlog.domain.repository

import com.rodrigonovoa.readlog.domain.model.Annotation
import kotlinx.coroutines.flow.Flow

interface AnnotationRepository {
    fun getAnnotationsForSession(sessionId: Int): Flow<List<Annotation>>
    suspend fun getAllAnnotationsListForSession(sessionId: Int): List<Annotation>
    suspend fun getAnnotationById(id: Int): Annotation?
    suspend fun getAnnotationByRemoteId(remoteId: String): Annotation?
    suspend fun insertAnnotation(annotation: Annotation)
    suspend fun updateAnnotation(annotation: Annotation)
    suspend fun deleteAnnotation(annotation: Annotation)
}
