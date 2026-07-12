package com.rodrigonovoa.readlog.domain.repository

import com.rodrigonovoa.readlog.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun getAllBooks(): Flow<List<Book>>
    suspend fun getAllBooksList(): List<Book>
    suspend fun getBookById(id: Int): Book?
    suspend fun getBookByRemoteId(remoteId: String): Book?
    suspend fun insertBook(book: Book)
    suspend fun updateBook(book: Book)
    suspend fun deleteBook(book: Book)
    suspend fun getBooksCount(): Int
}
