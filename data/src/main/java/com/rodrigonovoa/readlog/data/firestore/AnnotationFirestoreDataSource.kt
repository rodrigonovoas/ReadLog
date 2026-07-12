package com.rodrigonovoa.readlog.data.firestore

import com.rodrigonovoa.readlog.domain.model.Annotation

interface AnnotationFirestoreDataSource {
    suspend fun upload(userId: String, annotation: Annotation): Result<Unit>
    suspend fun downloadAll(userId: String): Result<List<Annotation>>
    suspend fun delete(userId: String, remoteId: String): Result<Unit>
}
