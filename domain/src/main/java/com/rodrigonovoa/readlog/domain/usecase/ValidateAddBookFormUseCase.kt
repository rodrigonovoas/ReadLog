package com.rodrigonovoa.readlog.domain.usecase

import javax.inject.Inject

class ValidateAddBookFormUseCase @Inject constructor() {
    operator fun invoke(
        title: String,
        pages: String,
        currentPage: String,
    ): Boolean {
        val pageNumber = pages.toIntOrNull()
        if (title.isBlank() || pageNumber == null || pageNumber <= 0) return false
        if (currentPage.isEmpty()) return true
        val currentPageNumber = currentPage.toIntOrNull()
        return currentPageNumber != null
                && currentPageNumber >= 0
                && currentPageNumber <= pageNumber
    }
}
