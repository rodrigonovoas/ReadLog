package com.rodrigonovoa.readlog.data.openlibrary

import com.rodrigonovoa.readlog.domain.model.BookMetadata
import javax.inject.Inject

class OpenLibraryBookMapperImpl @Inject constructor() : OpenLibraryBookMapper {

    override fun toDomain(
        isbnResponse: OpenLibraryIsbnResponse,
        authorNames: List<String>,
    ): BookMetadata {
        return BookMetadata(
            title = isbnResponse.title.orEmpty().trim(),
            author = authorNames.filter { it.isNotBlank() }.joinToString(", ").trim(),
            genre = isbnResponse.subjects?.firstOrNull { it.isNotBlank() }.orEmpty().trim(),
            releaseDate = extractYear(isbnResponse.publishDate),
            numPages = isbnResponse.numberOfPages,
        )
    }

    private fun extractYear(publishDate: String?): String {
        if (publishDate.isNullOrBlank()) return ""
        val yearMatch = Regex("""\b(19|20)\d{2}\b""").find(publishDate)
        return yearMatch?.value ?: publishDate.trim()
    }
}
