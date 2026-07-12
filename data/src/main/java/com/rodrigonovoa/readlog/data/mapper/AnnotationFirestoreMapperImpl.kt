package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.domain.model.Annotation
import javax.inject.Inject

class AnnotationFirestoreMapperImpl @Inject constructor() : AnnotationFirestoreMapper {

    override fun toFirestoreMap(annotation: Annotation): Map<String, Any> {
        return mapOf(
            "sessionRemoteId" to annotation.sessionRemoteId,
            "annotation" to annotation.annotation,
            "creationDate" to annotation.creationDate,
            "lastModified" to annotation.lastModified,
        )
    }

    override fun fromFirestoreMap(map: Map<String, Any?>, remoteId: String): Annotation {
        return Annotation(
            annotationId = 0,
            remoteId = remoteId,
            sessionId = 0,
            sessionRemoteId = map["sessionRemoteId"] as? String ?: "",
            annotation = map["annotation"] as? String ?: "",
            creationDate = (map["creationDate"] as? Number)?.toLong() ?: 0L,
            lastModified = (map["lastModified"] as? Number)?.toLong() ?: 0L,
        )
    }
}
