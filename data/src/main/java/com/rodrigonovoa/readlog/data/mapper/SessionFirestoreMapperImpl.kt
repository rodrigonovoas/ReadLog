package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.domain.model.Session
import javax.inject.Inject

class SessionFirestoreMapperImpl @Inject constructor() : SessionFirestoreMapper {

    override fun toFirestoreMap(session: Session): Map<String, Any> {
        return mapOf(
            "bookRemoteId" to session.bookRemoteId,
            "time" to session.time,
            "creationDate" to session.creationDate,
            "lastModified" to session.lastModified,
        )
    }

    override fun fromFirestoreMap(map: Map<String, Any?>, remoteId: String): Session {
        return Session(
            sessionId = 0,
            remoteId = remoteId,
            bookId = 0,
            bookRemoteId = map["bookRemoteId"] as? String ?: "",
            time = (map["time"] as? Number)?.toLong() ?: 0L,
            creationDate = (map["creationDate"] as? Number)?.toLong() ?: 0L,
            lastModified = (map["lastModified"] as? Number)?.toLong() ?: 0L,
        )
    }
}
