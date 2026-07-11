package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.data.db.entity.AnnotationEntity
import com.rodrigonovoa.readlog.domain.model.Annotation
import javax.inject.Inject

class AnnotationDataMapperImpl @Inject constructor() : AnnotationDataMapper {
    override fun toDomain(entity: AnnotationEntity): Annotation {
        return Annotation(
            annotationId = entity.annotationId,
            sessionId = entity.sessionId,
            annotation = entity.annotation,
            creationDate = entity.creationDate,
        )
    }

    override fun toEntity(domain: Annotation): AnnotationEntity {
        return AnnotationEntity(
            annotationId = domain.annotationId,
            sessionId = domain.sessionId,
            annotation = domain.annotation,
            creationDate = domain.creationDate,
        )
    }
}
