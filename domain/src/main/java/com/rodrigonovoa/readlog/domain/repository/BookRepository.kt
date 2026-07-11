package com.rodrigonovoa.readlog.domain.repository

import com.rodrigonovoa.readlog.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun getAllBooks(): Flow<List<Book>>
    suspend fun insertBook(book: Book)
    suspend fun getBooksCount(): Int
}
