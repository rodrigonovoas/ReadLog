package com.rodrigonovoa.readlog.domain.repository

import com.rodrigonovoa.readlog.domain.model.BookMetadata

interface BookMetadataRepository {
    suspend fun getByIsbn(isbn: String): Result<BookMetadata>
}
