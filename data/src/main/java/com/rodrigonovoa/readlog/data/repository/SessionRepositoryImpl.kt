package com.rodrigonovoa.readlog.data.repository

import com.rodrigonovoa.readlog.data.db.dao.BookDao
import com.rodrigonovoa.readlog.data.db.dao.SessionDao
import com.rodrigonovoa.readlog.data.firestore.SessionFirestoreDataSource
import com.rodrigonovoa.readlog.data.mapper.SessionDataMapper
import com.rodrigonovoa.readlog.domain.model.Session
import com.rodrigonovoa.readlog.domain.repository.AuthRepository
import com.rodrigonovoa.readlog.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val bookDao: BookDao,
    private val sessionDataMapper: SessionDataMapper,
    private val authRepository: AuthRepository,
    private val sessionFirestoreDataSource: SessionFirestoreDataSource,
) : SessionRepository {

    override fun getSessionsForBook(bookId: Int): Flow<List<Session>> {
        return sessionDao.getAllForBook(bookId).map { entities ->
            entities.map { sessionDataMapper.toDomain(it) }
        }
    }

    override suspend fun getAllSessionsListForBook(bookId: Int): List<Session> {
        return sessionDao.getAllListForBook(bookId).map { sessionDataMapper.toDomain(it) }
    }

    override suspend fun getAllSessionsSince(startOfWeekMillis: Long): List<Session> {
        return sessionDao.getSessionsSince(startOfWeekMillis).map { sessionDataMapper.toDomain(it) }
    }

    override suspend fun getSessionById(id: Int): Session? {
        return sessionDao.getById(id)?.let { sessionDataMapper.toDomain(it) }
    }

    override suspend fun getSessionByRemoteId(remoteId: String): Session? {
        return sessionDao.getByRemoteId(remoteId)?.let { sessionDataMapper.toDomain(it) }
    }

    override suspend fun insertSession(session: Session): Session {
        val enrichedSession = enrichSession(session)
        val entity = sessionDataMapper.toEntity(enrichedSession)
        val generatedId = sessionDao.insert(entity)
        val savedSession = enrichedSession.copy(sessionId = generatedId.toInt())
        pushToFirestore(savedSession)
        return savedSession
    }

    override suspend fun updateSession(session: Session) {
        val enrichedSession = enrichSession(session)
        val entity = sessionDataMapper.toEntity(enrichedSession)
        sessionDao.update(entity)
        pushToFirestore(enrichedSession)
    }

    override suspend fun deleteSession(session: Session) {
        val entity = sessionDataMapper.toEntity(session)
        sessionDao.delete(entity)
        authRepository.getCurrentUser()?.uid?.let { uid ->
            if (session.remoteId.isNotBlank()) {
                runCatching { sessionFirestoreDataSource.delete(uid, session.remoteId) }
            }
        }
    }

    private suspend fun enrichSession(session: Session): Session {
        val remoteId = if (session.remoteId.isBlank()) UUID.randomUUID().toString() else session.remoteId
        val bookRemoteId = if (session.bookRemoteId.isBlank()) {
            bookDao.getById(session.bookId)?.remoteId ?: ""
        } else session.bookRemoteId
        return session.copy(
            remoteId = remoteId,
            bookRemoteId = bookRemoteId,
            lastModified = System.currentTimeMillis(),
        )
    }

    private suspend fun pushToFirestore(session: Session) {
        authRepository.getCurrentUser()?.uid?.let { uid ->
            if (session.remoteId.isNotBlank() && session.bookRemoteId.isNotBlank()) {
                runCatching { sessionFirestoreDataSource.upload(uid, session) }
            }
        }
    }
}
