package com.rodrigonovoa.readlog.domain.repository

import com.rodrigonovoa.readlog.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getSessionsForBook(bookId: Int): Flow<List<Session>>
    suspend fun getAllSessionsListForBook(bookId: Int): List<Session>
    suspend fun getSessionById(id: Int): Session?
    suspend fun getSessionByRemoteId(remoteId: String): Session?
    suspend fun insertSession(session: Session)
    suspend fun updateSession(session: Session)
    suspend fun deleteSession(session: Session)
}
