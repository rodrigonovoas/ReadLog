package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.data.db.entity.SessionEntity
import com.rodrigonovoa.readlog.domain.model.Session
import javax.inject.Inject

class SessionDataMapperImpl @Inject constructor() : SessionDataMapper {
    override fun toDomain(entity: SessionEntity): Session {
        return Session(
            sessionId = entity.sessionId,
            remoteId = entity.remoteId,
            bookId = entity.bookId,
            bookRemoteId = entity.bookRemoteId,
            time = entity.time,
            creationDate = entity.creationDate,
            lastModified = entity.lastModified,
        )
    }

    override fun toEntity(domain: Session): SessionEntity {
        return SessionEntity(
            sessionId = domain.sessionId,
            remoteId = domain.remoteId,
            bookId = domain.bookId,
            bookRemoteId = domain.bookRemoteId,
            time = domain.time,
            creationDate = domain.creationDate,
            lastModified = domain.lastModified,
        )
    }
}
