package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.domain.model.Annotation

interface AnnotationFirestoreMapper {
    fun toFirestoreMap(annotation: Annotation): Map<String, Any>
    fun fromFirestoreMap(map: Map<String, Any?>, remoteId: String): Annotation
}
