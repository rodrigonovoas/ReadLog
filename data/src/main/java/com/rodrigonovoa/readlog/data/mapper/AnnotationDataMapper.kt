package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.data.db.entity.AnnotationEntity
import com.rodrigonovoa.readlog.domain.model.Annotation

interface AnnotationDataMapper {
    fun toDomain(entity: AnnotationEntity): Annotation
    fun toEntity(domain: Annotation): AnnotationEntity
}
