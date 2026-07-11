package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.data.db.entity.BookEntity
import com.rodrigonovoa.readlog.domain.model.Book
import javax.inject.Inject

class BookDataMapperImpl @Inject constructor() : BookDataMapper {
    override fun toDomain(entity: BookEntity): Book {
        return Book(
            bookId = entity.bookId,
            title = entity.title,
            author = entity.author,
            genre = entity.genre,
            releaseDate = entity.releaseDate,
            numPages = entity.numPages,
            currentPage = entity.currentPage,
            creationDate = entity.creationDate,
        )
    }

    override fun toEntity(domain: Book): BookEntity {
        return BookEntity(
            bookId = domain.bookId,
            title = domain.title,
            author = domain.author,
            genre = domain.genre,
            releaseDate = domain.releaseDate,
            numPages = domain.numPages,
            currentPage = domain.currentPage,
            creationDate = domain.creationDate,
        )
    }
}
