package com.rodrigonovoa.readlog.data.repository

import com.rodrigonovoa.readlog.data.db.dao.AnnotationDao
import com.rodrigonovoa.readlog.data.db.dao.BookDao
import com.rodrigonovoa.readlog.data.db.dao.SessionDao
import com.rodrigonovoa.readlog.data.db.entity.AnnotationEntity
import com.rodrigonovoa.readlog.data.db.entity.BookEntity
import com.rodrigonovoa.readlog.data.db.entity.SessionEntity
import com.rodrigonovoa.readlog.data.firestore.AnnotationFirestoreDataSource
import com.rodrigonovoa.readlog.data.firestore.BookFirestoreDataSource
import com.rodrigonovoa.readlog.data.firestore.SessionFirestoreDataSource
import com.rodrigonovoa.readlog.data.mapper.AnnotationDataMapper
import com.rodrigonovoa.readlog.data.mapper.BookDataMapper
import com.rodrigonovoa.readlog.data.mapper.SessionDataMapper
import com.rodrigonovoa.readlog.domain.model.Annotation
import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.model.Session
import com.rodrigonovoa.readlog.domain.repository.SyncRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val sessionDao: SessionDao,
    private val annotationDao: AnnotationDao,
    private val bookDataMapper: BookDataMapper,
    private val sessionDataMapper: SessionDataMapper,
    private val annotationDataMapper: AnnotationDataMapper,
    private val bookFirestoreDataSource: BookFirestoreDataSource,
    private val sessionFirestoreDataSource: SessionFirestoreDataSource,
    private val annotationFirestoreDataSource: AnnotationFirestoreDataSource,
) : SyncRepository {

    override suspend fun syncAll(userId: String): Result<Unit> {
        return try {
            val cloudBooksResult = bookFirestoreDataSource.downloadAll(userId)
            val cloudSessionsResult = sessionFirestoreDataSource.downloadAll(userId)
            val cloudAnnotationsResult = annotationFirestoreDataSource.downloadAll(userId)

            if (cloudBooksResult.isFailure) return Result.failure(cloudBooksResult.exceptionOrNull()!!)
            if (cloudSessionsResult.isFailure) return Result.failure(cloudSessionsResult.exceptionOrNull()!!)
            if (cloudAnnotationsResult.isFailure) return Result.failure(cloudAnnotationsResult.exceptionOrNull()!!)

            val cloudBooks = cloudBooksResult.getOrThrow()
            val cloudSessions = cloudSessionsResult.getOrThrow()
            val cloudAnnotations = cloudAnnotationsResult.getOrThrow()

            mergeBooks(cloudBooks)
            mergeSessions(cloudSessions)
            mergeAnnotations(cloudAnnotations)

            uploadBooks(userId, cloudBooks)
            uploadSessions(userId, cloudSessions)
            uploadAnnotations(userId, cloudAnnotations)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun mergeBooks(cloudBooks: List<Book>) {
        for (cloudBook in cloudBooks) {
            val localEntity = bookDao.getByRemoteId(cloudBook.remoteId)
            if (localEntity == null) {
                bookDao.insert(
                    BookEntity(
                        bookId = 0,
                        remoteId = cloudBook.remoteId,
                        title = cloudBook.title,
                        author = cloudBook.author,
                        genre = cloudBook.genre,
                        releaseDate = cloudBook.releaseDate,
                        numPages = cloudBook.numPages,
                        currentPage = cloudBook.currentPage,
                        creationDate = cloudBook.creationDate,
                        lastModified = cloudBook.lastModified,
                    )
                )
            } else if (cloudBook.lastModified > localEntity.lastModified) {
                bookDao.update(
                    localEntity.copy(
                        title = cloudBook.title,
                        author = cloudBook.author,
                        genre = cloudBook.genre,
                        releaseDate = cloudBook.releaseDate,
                        numPages = cloudBook.numPages,
                        currentPage = cloudBook.currentPage,
                        creationDate = cloudBook.creationDate,
                        lastModified = cloudBook.lastModified,
                    )
                )
            }
        }
    }

    private suspend fun mergeSessions(cloudSessions: List<Session>) {
        for (cloudSession in cloudSessions) {
            val localBook = bookDao.getByRemoteId(cloudSession.bookRemoteId)
            if (localBook == null) continue // orphan session, skip

            val localEntity = sessionDao.getByRemoteId(cloudSession.remoteId)
            if (localEntity == null) {
                sessionDao.insert(
                    SessionEntity(
                        sessionId = 0,
                        remoteId = cloudSession.remoteId,
                        bookId = localBook.bookId,
                        bookRemoteId = cloudSession.bookRemoteId,
                        time = cloudSession.time,
                        creationDate = cloudSession.creationDate,
                        lastModified = cloudSession.lastModified,
                    )
                )
            } else if (cloudSession.lastModified > localEntity.lastModified) {
                sessionDao.update(
                    localEntity.copy(
                        bookId = localBook.bookId,
                        bookRemoteId = cloudSession.bookRemoteId,
                        time = cloudSession.time,
                        creationDate = cloudSession.creationDate,
                        lastModified = cloudSession.lastModified,
                    )
                )
            }
        }
    }

    private suspend fun mergeAnnotations(cloudAnnotations: List<Annotation>) {
        for (cloudAnnotation in cloudAnnotations) {
            val localSession = sessionDao.getByRemoteId(cloudAnnotation.sessionRemoteId)
            if (localSession == null) continue // orphan annotation, skip

            val localEntity = annotationDao.getByRemoteId(cloudAnnotation.remoteId)
            if (localEntity == null) {
                annotationDao.insert(
                    AnnotationEntity(
                        annotationId = 0,
                        remoteId = cloudAnnotation.remoteId,
                        sessionId = localSession.sessionId,
                        sessionRemoteId = cloudAnnotation.sessionRemoteId,
                        annotation = cloudAnnotation.annotation,
                        creationDate = cloudAnnotation.creationDate,
                        lastModified = cloudAnnotation.lastModified,
                    )
                )
            } else if (cloudAnnotation.lastModified > localEntity.lastModified) {
                annotationDao.update(
                    localEntity.copy(
                        sessionId = localSession.sessionId,
                        sessionRemoteId = cloudAnnotation.sessionRemoteId,
                        annotation = cloudAnnotation.annotation,
                        creationDate = cloudAnnotation.creationDate,
                        lastModified = cloudAnnotation.lastModified,
                    )
                )
            }
        }
    }

    private suspend fun uploadBooks(userId: String, cloudBooks: List<Book>) {
        val cloudMap = cloudBooks.associateBy { it.remoteId }
        val localBooks = bookDao.getAllList()
        for (localEntity in localBooks) {
            val cloudBook = cloudMap[localEntity.remoteId]
            if (localEntity.lastModified > (cloudBook?.lastModified ?: Long.MIN_VALUE)) {
                val book = bookDataMapper.toDomain(localEntity)
                bookFirestoreDataSource.upload(userId, book)
            }
        }
    }

    private suspend fun uploadSessions(userId: String, cloudSessions: List<Session>) {
        val cloudMap = cloudSessions.associateBy { it.remoteId }
        val localSessions = sessionDao.getAllList()
        for (localEntity in localSessions) {
            val book = bookDao.getById(localEntity.bookId)
            if (book == null || book.remoteId.isBlank()) continue

            val cloudSession = cloudMap[localEntity.remoteId]
            if (localEntity.lastModified > (cloudSession?.lastModified ?: Long.MIN_VALUE)) {
                val session = sessionDataMapper.toDomain(localEntity).copy(
                    bookRemoteId = book.remoteId
                )
                sessionFirestoreDataSource.upload(userId, session)
            }
        }
    }

    private suspend fun uploadAnnotations(userId: String, cloudAnnotations: List<Annotation>) {
        val cloudMap = cloudAnnotations.associateBy { it.remoteId }
        val localAnnotations = annotationDao.getAllList()
        for (localEntity in localAnnotations) {
            val session = sessionDao.getById(localEntity.sessionId)
            if (session == null || session.remoteId.isBlank()) continue

            val cloudAnnotation = cloudMap[localEntity.remoteId]
            if (localEntity.lastModified > (cloudAnnotation?.lastModified ?: Long.MIN_VALUE)) {
                val annotation = annotationDataMapper.toDomain(localEntity).copy(
                    sessionRemoteId = session.remoteId
                )
                annotationFirestoreDataSource.upload(userId, annotation)
            }
        }
    }
}
