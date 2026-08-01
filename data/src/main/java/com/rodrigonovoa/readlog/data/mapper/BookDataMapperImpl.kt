package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.data.db.entity.BookEntity
import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.model.BookState
import javax.inject.Inject

class BookDataMapperImpl @Inject constructor() : BookDataMapper {
    override fun toDomain(entity: BookEntity): Book {
        return Book(
            bookId = entity.bookId,
            remoteId = entity.remoteId,
            title = entity.title,
            author = entity.author,
            genre = entity.genre,
            releaseDate = entity.releaseDate,
            numPages = entity.numPages,
            currentPage = entity.currentPage,
            state = parseBookState(entity.state),
            creationDate = entity.creationDate,
            lastModified = entity.lastModified,
            statusDate = entity.statusDate,
            coverUrl = entity.coverUrl,
        )
    }

    override fun toEntity(domain: Book): BookEntity {
        return BookEntity(
            bookId = domain.bookId,
            remoteId = domain.remoteId,
            title = domain.title,
            author = domain.author,
            genre = domain.genre,
            releaseDate = domain.releaseDate,
            numPages = domain.numPages,
            currentPage = domain.currentPage,
            state = domain.state.name,
            creationDate = domain.creationDate,
            lastModified = domain.lastModified,
            statusDate = domain.statusDate,
            coverUrl = domain.coverUrl,
        )
    }

    private fun parseBookState(value: String): BookState {
        return try {
            BookState.valueOf(value)
        } catch (_: IllegalArgumentException) {
            BookState.IN_PROGRESS
        }
    }
}
