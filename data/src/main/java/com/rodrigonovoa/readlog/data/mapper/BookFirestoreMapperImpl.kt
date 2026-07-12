package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.domain.model.Book
import javax.inject.Inject

class BookFirestoreMapperImpl @Inject constructor() : BookFirestoreMapper {

    override fun toFirestoreMap(book: Book): Map<String, Any> {
        return mapOf(
            "title" to book.title,
            "author" to book.author,
            "genre" to book.genre,
            "releaseDate" to book.releaseDate,
            "numPages" to book.numPages,
            "currentPage" to book.currentPage,
            "creationDate" to book.creationDate,
            "lastModified" to book.lastModified,
        )
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
            creationDate = (map["creationDate"] as? Number)?.toLong() ?: 0L,
            lastModified = (map["lastModified"] as? Number)?.toLong() ?: 0L,
        )
    }
}
