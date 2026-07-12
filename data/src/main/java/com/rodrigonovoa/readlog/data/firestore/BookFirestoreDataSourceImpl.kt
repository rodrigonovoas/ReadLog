package com.rodrigonovoa.readlog.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.rodrigonovoa.readlog.data.mapper.BookFirestoreMapper
import com.rodrigonovoa.readlog.domain.model.Book
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookFirestoreDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val bookFirestoreMapper: BookFirestoreMapper,
) : BookFirestoreDataSource {

    override suspend fun upload(userId: String, book: Book): Result<Unit> {
        return try {
            firestore
                .collection("users")
                .document(userId)
                .collection("books")
                .document(book.remoteId)
                .set(bookFirestoreMapper.toFirestoreMap(book))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadAll(userId: String): Result<List<Book>> {
        return try {
            val snapshot = firestore
                .collection("users")
                .document(userId)
                .collection("books")
                .get()
                .await()
            val books = snapshot.documents.map { doc ->
                bookFirestoreMapper.fromFirestoreMap(
                    doc.data ?: emptyMap(),
                    doc.id
                )
            }
            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun delete(userId: String, remoteId: String): Result<Unit> {
        return try {
            firestore
                .collection("users")
                .document(userId)
                .collection("books")
                .document(remoteId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
