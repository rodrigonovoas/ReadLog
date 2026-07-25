package com.rodrigonovoa.readlog.data.openlibrary

import com.rodrigonovoa.readlog.domain.model.BookMetadata
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenLibraryBookMapperImplTest {

    private val mapper = OpenLibraryBookMapperImpl()

    @Test
    fun `toDomain maps full response to BookMetadata`() {
        val isbnResponse = OpenLibraryIsbnResponse(
            title = "The Hobbit",
            authors = listOf(OpenLibraryAuthorRef("/authors/OL12345A")),
            numberOfPages = 310,
            publishDate = "21 September 1937",
            subjects = listOf("Fantasy", "Adventure"),
        )

        val result = mapper.toDomain(isbnResponse, listOf("J. R. R. Tolkien"))

        assertEquals(
            BookMetadata(
                title = "The Hobbit",
                author = "J. R. R. Tolkien",
                genre = "Fantasy",
                releaseDate = "1937",
                numPages = 310,
            ),
            result,
        )
    }

    @Test
    fun `toDomain joins multiple authors`() {
        val isbnResponse = OpenLibraryIsbnResponse(
            title = "Book",
            authors = listOf(OpenLibraryAuthorRef("/authors/OL1A"), OpenLibraryAuthorRef("/authors/OL2A")),
            numberOfPages = null,
            publishDate = null,
            subjects = null,
        )

        val result = mapper.toDomain(isbnResponse, listOf("Author One", "Author Two"))

        assertEquals("Author One, Author Two", result.author)
    }

    @Test
    fun `toDomain handles missing fields gracefully`() {
        val isbnResponse = OpenLibraryIsbnResponse(
            title = null,
            authors = null,
            numberOfPages = null,
            publishDate = null,
            subjects = null,
        )

        val result = mapper.toDomain(isbnResponse, emptyList())

        assertEquals("", result.title)
        assertEquals("", result.author)
        assertEquals("", result.genre)
        assertEquals("", result.releaseDate)
        assertEquals(null, result.numPages)
    }

    @Test
    fun `toDomain extracts year from publish date`() {
        val isbnResponse = OpenLibraryIsbnResponse(
            title = "Book",
            authors = null,
            numberOfPages = null,
            publishDate = "Published: October 2005 by Some Publisher",
            subjects = null,
        )

        val result = mapper.toDomain(isbnResponse, emptyList())

        assertEquals("2005", result.releaseDate)
    }

    @Test
    fun `toDomain keeps raw publish date when no year found`() {
        val isbnResponse = OpenLibraryIsbnResponse(
            title = "Book",
            authors = null,
            numberOfPages = null,
            publishDate = "Ancient times",
            subjects = null,
        )

        val result = mapper.toDomain(isbnResponse, emptyList())

        assertEquals("Ancient times", result.releaseDate)
    }
}
