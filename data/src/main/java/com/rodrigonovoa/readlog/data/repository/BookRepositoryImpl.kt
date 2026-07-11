package com.rodrigonovoa.readlog.data.repository

import com.rodrigonovoa.readlog.data.db.dao.BookDao
import com.rodrigonovoa.readlog.data.db.entity.BookEntity
import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao
) : BookRepository {

    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertBook(book: Book) {
        bookDao.insert(book.toEntity())
    }

    override suspend fun getBooksCount(): Int {
        return bookDao.getAllCount()
    }
}

private fun BookEntity.toDomain(): Book {
    return Book(
        bookId = bookId,
        title = title,
        author = author,
        genre = genre,
        releaseDate = releaseDate,
        numPages = numPages,
        currentPage = currentPage,
        creationDate = creationDate,
    )
}

private fun Book.toEntity(): BookEntity {
    return BookEntity(
        bookId = bookId,
        title = title,
        author = author,
        genre = genre,
        releaseDate = releaseDate,
        numPages = numPages,
        currentPage = currentPage,
        creationDate = creationDate,
    )
}
