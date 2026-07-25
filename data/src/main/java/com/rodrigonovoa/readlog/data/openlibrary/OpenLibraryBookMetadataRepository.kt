package com.rodrigonovoa.readlog.data.openlibrary

import com.rodrigonovoa.readlog.domain.model.BookMetadata
import com.rodrigonovoa.readlog.domain.repository.BookMetadataRepository
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenLibraryBookMetadataRepository @Inject constructor(
    private val openLibraryApi: OpenLibraryApi,
    private val mapper: OpenLibraryBookMapper,
) : BookMetadataRepository {

    override suspend fun getByIsbn(isbn: String): Result<BookMetadata> {
        return runCatching {
            val isbnResponse = try {
                openLibraryApi.getBookByIsbn(isbn)
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    throw NoSuchElementException("Book not found for ISBN: $isbn")
                } else {
                    throw e
                }
            }

            if (isbnResponse.title.isNullOrBlank()) {
                throw NoSuchElementException("Book not found for ISBN: $isbn")
            }

            val authorNames = isbnResponse.authors
                .orEmpty()
                .mapNotNull { it?.key }
                .map { key ->
                    val authorKey = key.removePrefix("/")
                    runCatching {
                        openLibraryApi.getAuthor(authorKey).name.orEmpty()
                    }.getOrDefault("")
                }

            mapper.toDomain(isbnResponse, authorNames)
        }
    }
}
