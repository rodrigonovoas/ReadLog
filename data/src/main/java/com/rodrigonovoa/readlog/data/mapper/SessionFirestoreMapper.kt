package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.domain.model.Session

interface SessionFirestoreMapper {
    fun toFirestoreMap(session: Session): Map<String, Any>
    fun fromFirestoreMap(map: Map<String, Any?>, remoteId: String): Session
}
