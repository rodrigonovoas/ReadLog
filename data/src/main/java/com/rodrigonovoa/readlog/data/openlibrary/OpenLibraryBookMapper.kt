package com.rodrigonovoa.readlog.data.openlibrary

import com.rodrigonovoa.readlog.domain.model.BookMetadata

interface OpenLibraryBookMapper {
    fun toDomain(isbnResponse: OpenLibraryIsbnResponse, authorNames: List<String>): BookMetadata
}
