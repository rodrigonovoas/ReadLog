package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.BookMetadata
import com.rodrigonovoa.readlog.domain.repository.BookMetadataRepository
import javax.inject.Inject

class FetchBookByIsbnUseCase @Inject constructor(
    private val bookMetadataRepository: BookMetadataRepository,
) {
    suspend operator fun invoke(isbn: String): Result<BookMetadata> {
        val cleanedIsbn = isbn.replace(Regex("[^0-9A-Za-z]"), "")
        if (cleanedIsbn.isBlank()) {
            return Result.failure(IllegalArgumentException("ISBN is empty"))
        }
        return bookMetadataRepository.getByIsbn(cleanedIsbn)
    }
}
