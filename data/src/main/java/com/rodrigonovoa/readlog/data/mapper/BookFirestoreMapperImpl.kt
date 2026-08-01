package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.model.BookState
import javax.inject.Inject

class BookFirestoreMapperImpl @Inject constructor() : BookFirestoreMapper {

    override fun toFirestoreMap(book: Book): Map<String, Any> {
        return buildMap {
            put("title", book.title)
            put("author", book.author)
            put("genre", book.genre)
            put("releaseDate", book.releaseDate)
            put("numPages", book.numPages)
            put("currentPage", book.currentPage)
            put("state", book.state.name)
            put("creationDate", book.creationDate)
            put("lastModified", book.lastModified)
            book.statusDate?.let { put("statusDate", it) }
        }
    }

    override fun fromFirestoreMap(map: Map<String, Any?>, remoteId: String): Book {
        return Book(
            bookId = 0,
            remoteId = remoteId,
            title = map["title"] as? String ?: "",
            author = map["author"] as? String ?: "",
            genre = map["genre"] as? String ?: "",
            releaseDate = map["releaseDate"] as? String ?: "",
            numPages = (map["numPages"] as? Number)?.toInt() ?: 0,
            currentPage = (map["currentPage"] as? Number)?.toInt() ?: 0,
            state = parseBookState(map["state"] as? String),
            creationDate = (map["creationDate"] as? Number)?.toLong() ?: 0L,
            lastModified = (map["lastModified"] as? Number)?.toLong() ?: 0L,
            statusDate = (map["statusDate"] as? Number)?.toLong(),
        )
    }

    private fun parseBookState(value: String?): BookState {
        return try {
            value?.let { BookState.valueOf(it) } ?: BookState.IN_PROGRESS
        } catch (_: IllegalArgumentException) {
            BookState.IN_PROGRESS
        }
    }
}
