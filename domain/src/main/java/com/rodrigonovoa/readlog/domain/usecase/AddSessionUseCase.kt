package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.Session
import com.rodrigonovoa.readlog.domain.repository.SessionRepository
import javax.inject.Inject

class AddSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(bookId: Int, time: Long, creationDate: Long = System.currentTimeMillis()): Result<Session> {
        val session = Session(bookId = bookId, time = time, creationDate = creationDate)
        return runCatching { sessionRepository.insertSession(session) }
    }
}
