package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.BookMetadata
import com.rodrigonovoa.readlog.domain.repository.BookMetadataRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FetchBookByIsbnUseCaseTest {

    private lateinit var bookMetadataRepository: BookMetadataRepository
    private lateinit var useCase: FetchBookByIsbnUseCase

    @Before
    fun setup() {
        bookMetadataRepository = mockk()
        useCase = FetchBookByIsbnUseCase(bookMetadataRepository)
    }

    @Test
    fun `invoke cleans isbn and returns metadata`() = runTest {
        val metadata = BookMetadata(
            title = "Title",
            author = "Author",
            genre = "Genre",
            releaseDate = "2020",
            numPages = 100,
            coverUrl = "",
        )
        coEvery { bookMetadataRepository.getByIsbn("9781234567890") } returns Result.success(metadata)

        val result = useCase("978-1-234-56789-0")

        assertTrue(result.isSuccess)
        assertEquals(metadata, result.getOrNull())
        coVerify { bookMetadataRepository.getByIsbn("9781234567890") }
    }

    @Test
    fun `invoke returns failure for blank isbn`() = runTest {
        val result = useCase("   ")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}
