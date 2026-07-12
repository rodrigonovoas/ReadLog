package com.rodrigonovoa.readlog.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.rodrigonovoa.readlog.data.mapper.SessionFirestoreMapper
import com.rodrigonovoa.readlog.domain.model.Session
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionFirestoreDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val sessionFirestoreMapper: SessionFirestoreMapper,
) : SessionFirestoreDataSource {

    override suspend fun upload(userId: String, session: Session): Result<Unit> {
        return try {
            firestore
                .collection("users")
                .document(userId)
                .collection("sessions")
                .document(session.remoteId)
                .set(sessionFirestoreMapper.toFirestoreMap(session))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadAll(userId: String): Result<List<Session>> {
        return try {
            val snapshot = firestore
                .collection("users")
                .document(userId)
                .collection("sessions")
                .get()
                .await()
            val sessions = snapshot.documents.map { doc ->
                sessionFirestoreMapper.fromFirestoreMap(
                    doc.data ?: emptyMap(),
                    doc.id
                )
            }
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun delete(userId: String, remoteId: String): Result<Unit> {
        return try {
            firestore
                .collection("users")
                .document(userId)
                .collection("sessions")
                .document(remoteId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
