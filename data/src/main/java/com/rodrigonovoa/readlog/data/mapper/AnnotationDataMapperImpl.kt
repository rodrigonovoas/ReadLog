package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.data.db.entity.AnnotationEntity
import com.rodrigonovoa.readlog.domain.model.Annotation
import javax.inject.Inject

class AnnotationDataMapperImpl @Inject constructor() : AnnotationDataMapper {
    override fun toDomain(entity: AnnotationEntity): Annotation {
        return Annotation(
            annotationId = entity.annotationId,
            remoteId = entity.remoteId,
            sessionId = entity.sessionId,
            sessionRemoteId = entity.sessionRemoteId,
            annotation = entity.annotation,
            creationDate = entity.creationDate,
            lastModified = entity.lastModified,
        )
    }

    override fun toEntity(domain: Annotation): AnnotationEntity {
        return AnnotationEntity(
            annotationId = domain.annotationId,
            remoteId = domain.remoteId,
            sessionId = domain.sessionId,
            sessionRemoteId = domain.sessionRemoteId,
            annotation = domain.annotation,
            creationDate = domain.creationDate,
            lastModified = domain.lastModified,
        )
    }
}
