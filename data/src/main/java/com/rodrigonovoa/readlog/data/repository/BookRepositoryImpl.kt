package com.rodrigonovoa.readlog.data.repository

import com.rodrigonovoa.readlog.data.db.dao.BookDao
import com.rodrigonovoa.readlog.data.mapper.BookDataMapper
import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val bookDataMapper: BookDataMapper,
) : BookRepository {

    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAll().map { entities ->
            entities.map { bookDataMapper.toDomain(it) }
        }
    }

    override suspend fun insertBook(book: Book) {
        bookDao.insert(bookDataMapper.toEntity(book))
    }

    override suspend fun getBooksCount(): Int {
        return bookDao.getAllCount()
    }
}
