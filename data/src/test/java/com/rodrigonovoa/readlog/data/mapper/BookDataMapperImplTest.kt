package com.rodrigonovoa.readlog.data.mapper

import com.rodrigonovoa.readlog.data.db.entity.BookEntity
import com.rodrigonovoa.readlog.domain.model.Book
import org.junit.Assert.assertEquals
import org.junit.Test

class BookDataMapperImplTest {

    private val mapper = BookDataMapperImpl()

    @Test
    fun `toDomain maps entity to domain model`() {
        val entity = BookEntity(
            bookId = 1,
            title = "Cien años de soledad",
            author = "Gabriel García Márquez",
            genre = "Novel",
            releaseDate = "1967",
            numPages = 340,
            currentPage = 231,
            creationDate = 12345678L,
        )

        val result = mapper.toDomain(entity)

        assertEquals(
            Book(
                bookId = 1,
                title = "Cien años de soledad",
                author = "Gabriel García Márquez",
                genre = "Novel",
                releaseDate = "1967",
                numPages = 340,
                currentPage = 231,
                creationDate = 12345678L,
            ),
            result
        )
    }

    @Test
    fun `toEntity maps domain model to entity`() {
        val domain = Book(
            bookId = 2,
            title = "Las palabras y las cosas",
            author = "Michel Foucault",
            genre = "Philosophy",
            releaseDate = "1966",
            numPages = 300,
            currentPage = 102,
            creationDate = 87654321L,
        )

        val result = mapper.toEntity(domain)

        assertEquals(
            BookEntity(
                bookId = 2,
                title = "Las palabras y las cosas",
                author = "Michel Foucault",
                genre = "Philosophy",
                releaseDate = "1966",
                numPages = 300,
                currentPage = 102,
                creationDate = 87654321L,
            ),
            result
        )
    }

    @Test
    fun `roundtrip conversion preserves data`() {
        val original = Book(
            bookId = 3,
            title = "El nombre del viento",
            author = "Patrick Rothfuss",
            genre = "Fantasy",
            releaseDate = "2007",
            numPages = 662,
            currentPage = 80,
            creationDate = 11111111L,
        )

        val entity = mapper.toEntity(original)
        val roundtrip = mapper.toDomain(entity)

        assertEquals(original, roundtrip)
    }
}
