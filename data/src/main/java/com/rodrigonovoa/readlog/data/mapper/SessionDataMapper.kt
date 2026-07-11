package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.data.db.entity.SessionEntity
import com.rodrigonovoa.readlog.domain.model.Session

interface SessionDataMapper {
    fun toDomain(entity: SessionEntity): Session
    fun toEntity(domain: Session): SessionEntity
}
