package com.rodrigonovoa.readlog.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.rodrigonovoa.readlog.data.mapper.AnnotationFirestoreMapper
import com.rodrigonovoa.readlog.domain.model.Annotation
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnotationFirestoreDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val annotationFirestoreMapper: AnnotationFirestoreMapper,
) : AnnotationFirestoreDataSource {

    override suspend fun upload(userId: String, annotation: Annotation): Result<Unit> {
        return try {
            firestore
                .collection("users")
                .document(userId)
                .collection("annotations")
                .document(annotation.remoteId)
                .set(annotationFirestoreMapper.toFirestoreMap(annotation))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadAll(userId: String): Result<List<Annotation>> {
        return try {
            val snapshot = firestore
                .collection("users")
                .document(userId)
                .collection("annotations")
                .get()
                .await()
            val annotations = snapshot.documents.map { doc ->
                annotationFirestoreMapper.fromFirestoreMap(
                    doc.data ?: emptyMap(),
                    doc.id
                )
            }
            Result.success(annotations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun delete(userId: String, remoteId: String): Result<Unit> {
        return try {
            firestore
                .collection("users")
                .document(userId)
                .collection("annotations")
                .document(remoteId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
