package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.model.BookState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
            state = BookState.PAUSED,
            creationDate = 1000L,
            lastModified = 2000L,
            statusDate = 3000L,
            coverUrl = "https://covers.openlibrary.org/b/id/1-S.jpg",
        )

        val map = mapper.toFirestoreMap(book)

        assertEquals("Title", map["title"])
        assertEquals("Author", map["author"])
        assertEquals("Genre", map["genre"])
        assertEquals("2024", map["releaseDate"])
        assertEquals(300, map["numPages"])
        assertEquals(50, map["currentPage"])
        assertEquals(BookState.PAUSED.name, map["state"])
        assertEquals(1000L, map["creationDate"])
        assertEquals(2000L, map["lastModified"])
        assertEquals(3000L, map["statusDate"])
        assertEquals("https://covers.openlibrary.org/b/id/1-S.jpg", map["coverUrl"])
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
                state = BookState.IN_PROGRESS,
                creationDate = 0L,
                lastModified = 5000L,
                statusDate = null,
                coverUrl = "",
            ),
            book
        )
    }

    @Test
    fun `fromFirestoreMap reads state when present`() {
        val map = mapOf(
            "title" to "Title",
            "author" to "Author",
            "numPages" to 100,
            "currentPage" to 10,
            "state" to BookState.COMPLETED.name,
            "lastModified" to 5000L,
            "statusDate" to 6000L,
        )

        val book = mapper.fromFirestoreMap(map, "uuid-3")

        assertEquals(BookState.COMPLETED, book.state)
        assertEquals(6000L, book.statusDate)
        assertEquals("", book.coverUrl)
    }

    @Test
    fun `toFirestoreMap omits statusDate when null`() {
        val book = Book(
            bookId = 1,
            remoteId = "uuid-1",
            title = "Title",
            author = "Author",
            genre = "Genre",
            releaseDate = "2024",
            numPages = 300,
            currentPage = 50,
            state = BookState.IN_PROGRESS,
            creationDate = 1000L,
            lastModified = 2000L,
            statusDate = null,
            coverUrl = "",
        )

        val map = mapper.toFirestoreMap(book)

        assertFalse("statusDate" in map)
        assertEquals("", map["coverUrl"])
    }
}
