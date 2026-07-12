package com.rodrigonovoa.readlog.data.repository

import com.rodrigonovoa.readlog.data.db.dao.AnnotationDao
import com.rodrigonovoa.readlog.data.db.dao.SessionDao
import com.rodrigonovoa.readlog.data.firestore.AnnotationFirestoreDataSource
import com.rodrigonovoa.readlog.data.mapper.AnnotationDataMapper
import com.rodrigonovoa.readlog.domain.model.Annotation
import com.rodrigonovoa.readlog.domain.repository.AnnotationRepository
import com.rodrigonovoa.readlog.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnotationRepositoryImpl @Inject constructor(
    private val annotationDao: AnnotationDao,
    private val sessionDao: SessionDao,
    private val annotationDataMapper: AnnotationDataMapper,
    private val authRepository: AuthRepository,
    private val annotationFirestoreDataSource: AnnotationFirestoreDataSource,
) : AnnotationRepository {

    override fun getAnnotationsForSession(sessionId: Int): Flow<List<Annotation>> {
        return annotationDao.getAllForSession(sessionId).map { entities ->
            entities.map { annotationDataMapper.toDomain(it) }
        }
    }

    override suspend fun getAllAnnotationsListForSession(sessionId: Int): List<Annotation> {
        return annotationDao.getAllListForSession(sessionId).map { annotationDataMapper.toDomain(it) }
    }

    override suspend fun getAnnotationById(id: Int): Annotation? {
        return annotationDao.getById(id)?.let { annotationDataMapper.toDomain(it) }
    }

    override suspend fun getAnnotationByRemoteId(remoteId: String): Annotation? {
        return annotationDao.getByRemoteId(remoteId)?.let { annotationDataMapper.toDomain(it) }
    }

    override suspend fun insertAnnotation(annotation: Annotation) {
        val enrichedAnnotation = enrichAnnotation(annotation)
        val entity = annotationDataMapper.toEntity(enrichedAnnotation)
        annotationDao.insert(entity)
        pushToFirestore(enrichedAnnotation)
    }

    override suspend fun updateAnnotation(annotation: Annotation) {
        val enrichedAnnotation = enrichAnnotation(annotation)
        val entity = annotationDataMapper.toEntity(enrichedAnnotation)
        annotationDao.update(entity)
        pushToFirestore(enrichedAnnotation)
    }

    override suspend fun deleteAnnotation(annotation: Annotation) {
        val entity = annotationDataMapper.toEntity(annotation)
        annotationDao.delete(entity)
        authRepository.getCurrentUser()?.uid?.let { uid ->
            if (annotation.remoteId.isNotBlank()) {
                runCatching { annotationFirestoreDataSource.delete(uid, annotation.remoteId) }
            }
        }
    }

    private suspend fun enrichAnnotation(annotation: Annotation): Annotation {
        val remoteId = if (annotation.remoteId.isBlank()) UUID.randomUUID().toString() else annotation.remoteId
        val sessionRemoteId = if (annotation.sessionRemoteId.isBlank()) {
            sessionDao.getById(annotation.sessionId)?.remoteId ?: ""
        } else annotation.sessionRemoteId
        return annotation.copy(
            remoteId = remoteId,
            sessionRemoteId = sessionRemoteId,
            lastModified = System.currentTimeMillis(),
        )
    }

    private suspend fun pushToFirestore(annotation: Annotation) {
        authRepository.getCurrentUser()?.uid?.let { uid ->
            if (annotation.remoteId.isNotBlank() && annotation.sessionRemoteId.isNotBlank()) {
                runCatching { annotationFirestoreDataSource.upload(uid, annotation) }
            }
        }
    }
}
