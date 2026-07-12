package com.rodrigonovoa.readlog.data.repository

import com.rodrigonovoa.readlog.data.db.dao.BookDao
import com.rodrigonovoa.readlog.data.firestore.BookFirestoreDataSource
import com.rodrigonovoa.readlog.data.mapper.BookDataMapper
import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.repository.AuthRepository
import com.rodrigonovoa.readlog.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val bookDataMapper: BookDataMapper,
    private val authRepository: AuthRepository,
    private val bookFirestoreDataSource: BookFirestoreDataSource,
) : BookRepository {

    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAll().map { entities ->
            entities.map { bookDataMapper.toDomain(it) }
        }
    }

    override suspend fun getAllBooksList(): List<Book> {
        return bookDao.getAllList().map { bookDataMapper.toDomain(it) }
    }

    override suspend fun getBookById(id: Int): Book? {
        return bookDao.getById(id)?.let { bookDataMapper.toDomain(it) }
    }

    override suspend fun getBookByRemoteId(remoteId: String): Book? {
        return bookDao.getByRemoteId(remoteId)?.let { bookDataMapper.toDomain(it) }
    }

    override suspend fun insertBook(book: Book) {
        val enrichedBook = if (book.remoteId.isBlank()) {
            book.copy(remoteId = UUID.randomUUID().toString(), lastModified = System.currentTimeMillis())
        } else book
        val entity = bookDataMapper.toEntity(enrichedBook)
        bookDao.insert(entity)
        pushToFirestore(enrichedBook)
    }

    override suspend fun updateBook(book: Book) {
        val enrichedBook = if (book.remoteId.isBlank()) {
            book.copy(remoteId = UUID.randomUUID().toString(), lastModified = System.currentTimeMillis())
        } else {
            book.copy(lastModified = System.currentTimeMillis())
        }
        val entity = bookDataMapper.toEntity(enrichedBook)
        bookDao.update(entity)
        pushToFirestore(enrichedBook)
    }

    override suspend fun deleteBook(book: Book) {
        val entity = bookDataMapper.toEntity(book)
        bookDao.delete(entity)
        authRepository.getCurrentUser()?.uid?.let { uid ->
            if (book.remoteId.isNotBlank()) {
                runCatching { bookFirestoreDataSource.delete(uid, book.remoteId) }
            }
        }
    }

    private suspend fun pushToFirestore(book: Book) {
        authRepository.getCurrentUser()?.uid?.let { uid ->
            if (book.remoteId.isNotBlank()) {
                runCatching { bookFirestoreDataSource.upload(uid, book) }
            }
        }
    }

    override suspend fun getBooksCount(): Int {
        return bookDao.getAllCount()
    }
}
