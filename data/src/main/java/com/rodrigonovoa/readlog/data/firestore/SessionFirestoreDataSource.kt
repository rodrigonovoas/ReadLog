package com.rodrigonovoa.readlog.data.firestore

import com.rodrigonovoa.readlog.domain.model.Session

interface SessionFirestoreDataSource {
    suspend fun upload(userId: String, session: Session): Result<Unit>
    suspend fun downloadAll(userId: String): Result<List<Session>>
    suspend fun delete(userId: String, remoteId: String): Result<Unit>
}
