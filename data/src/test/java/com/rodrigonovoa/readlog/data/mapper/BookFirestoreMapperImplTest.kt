package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.domain.model.Book
import org.junit.Assert.assertEquals
import org.junit.Test

class BookFirestoreMapperImplTest {

    private val mapper = BookFirestoreMapperImpl()

    @Test
    fun `toFirestoreMap produces correct map`() {
        val book = Book(
            bookId = 1,
            remoteId = "uuid-1",
            title = "Title",
            author = "Author",
            genre = "Genre",
            releaseDate = "2024",
            numPages = 300,
            currentPage = 50,
            creationDate = 1000L,
            lastModified = 2000L,
        )

        val map = mapper.toFirestoreMap(book)

        assertEquals("Title", map["title"])
        assertEquals("Author", map["author"])
        assertEquals("Genre", map["genre"])
        assertEquals("2024", map["releaseDate"])
        assertEquals(300, map["numPages"])
        assertEquals(50, map["currentPage"])
        assertEquals(1000L, map["creationDate"])
        assertEquals(2000L, map["lastModified"])
    }

    @Test
    fun `fromFirestoreMap reconstructs Book with defaults for missing fields`() {
        val map = mapOf(
            "title" to "Title",
            "author" to "Author",
            "numPages" to 100,
            "currentPage" to 10,
            "lastModified" to 5000L,
        )

        val book = mapper.fromFirestoreMap(map, "uuid-2")

        assertEquals(
            Book(
                bookId = 0,
                remoteId = "uuid-2",
                title = "Title",
                author = "Author",
                genre = "",
                releaseDate = "",
                numPages = 100,
                currentPage = 10,
                creationDate = 0L,
                lastModified = 5000L,
            ),
            book
        )
    }
}
