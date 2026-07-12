package com.rodrigonovoa.readlog.data.firestore

import com.rodrigonovoa.readlog.domain.model.Book

interface BookFirestoreDataSource {
    suspend fun upload(userId: String, book: Book): Result<Unit>
    suspend fun downloadAll(userId: String): Result<List<Book>>
    suspend fun delete(userId: String, remoteId: String): Result<Unit>
}
