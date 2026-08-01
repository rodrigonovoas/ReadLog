package com.rodrigonovoa.readlog.data.openlibrary

import com.rodrigonovoa.readlog.domain.model.BookMetadata
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class OpenLibraryBookMetadataRepositoryTest {

    private lateinit var openLibraryApi: OpenLibraryApi
    private lateinit var mapper: OpenLibraryBookMapper
    private lateinit var repository: OpenLibraryBookMetadataRepository

    @Before
    fun setup() {
        openLibraryApi = mockk()
        mapper = mockk()
        repository = OpenLibraryBookMetadataRepository(openLibraryApi, mapper)
    }

    @Test
    fun `getByIsbn fetches book and author then maps to domain`() = runTest {
        val isbnResponse = OpenLibraryIsbnResponse(
            title = "Dune",
            authors = listOf(OpenLibraryAuthorRef("/authors/OL123A")),
            numberOfPages = 412,
            publishDate = "1965",
            subjects = listOf("Science fiction"),
        )
        val authorResponse = OpenLibraryAuthorResponse(name = "Frank Herbert")
        val metadata = BookMetadata(
            title = "Dune",
            author = "Frank Herbert",
            genre = "Science fiction",
            releaseDate = "1965",
            numPages = 412,
            coverUrl = "",
        )

        coEvery { openLibraryApi.getBookByIsbn("9780441172719") } returns isbnResponse
        coEvery { openLibraryApi.getAuthor("authors/OL123A") } returns authorResponse
        coEvery { mapper.toDomain(isbnResponse, listOf("Frank Herbert")) } returns metadata

        val result = repository.getByIsbn("9780441172719")

        assertTrue(result.isSuccess)
        assertEquals(metadata, result.getOrNull())
    }

    @Test
    fun `getByIsbn maps 404 to NoSuchElementException`() = runTest {
        val exception = HttpException(
            Response.error<Any>(404, ResponseBody.create(null, ""))
        )
        coEvery { openLibraryApi.getBookByIsbn("9780000000000") } throws exception

        val result = repository.getByIsbn("9780000000000")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `getByIsbn returns failure when title is blank`() = runTest {
        val isbnResponse = OpenLibraryIsbnResponse(
            title = null,
            authors = null,
            numberOfPages = null,
            publishDate = null,
            subjects = null,
        )
        coEvery { openLibraryApi.getBookByIsbn("9781234567890") } returns isbnResponse

        val result = repository.getByIsbn("9781234567890")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `getByIsbn ignores failing author fetches`() = runTest {
        val isbnResponse = OpenLibraryIsbnResponse(
            title = "Book",
            authors = listOf(OpenLibraryAuthorRef("/authors/OL1A")),
            numberOfPages = 100,
            publishDate = "2020",
            subjects = null,
        )
        val metadata = BookMetadata(
            title = "Book",
            author = "",
            genre = "",
            releaseDate = "2020",
            numPages = 100,
            coverUrl = "",
        )

        coEvery { openLibraryApi.getBookByIsbn("9781234567890") } returns isbnResponse
        coEvery { openLibraryApi.getAuthor("authors/OL1A") } throws RuntimeException("network error")
        coEvery { mapper.toDomain(isbnResponse, listOf("")) } returns metadata

        val result = repository.getByIsbn("9781234567890")

        assertTrue(result.isSuccess)
        assertEquals(metadata, result.getOrNull())
    }
}
