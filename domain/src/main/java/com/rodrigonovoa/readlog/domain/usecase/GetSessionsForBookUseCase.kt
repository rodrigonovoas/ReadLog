package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.Session
import com.rodrigonovoa.readlog.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSessionsForBookUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(bookId: Int): Flow<List<Session>> {
        return sessionRepository.getSessionsForBook(bookId)
    }
}
