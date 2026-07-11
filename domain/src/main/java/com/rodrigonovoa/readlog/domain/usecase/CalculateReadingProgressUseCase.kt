package com.rodrigonovoa.readlog.domain.usecase

import javax.inject.Inject

class CalculateReadingProgressUseCase @Inject constructor() {
    operator fun invoke(
        currentPageStr: String,
        pagesStr: String,
    ): Int {
        if (currentPageStr.isEmpty()) return 0
        val currentPage = currentPageStr.toIntOrNull() ?: return 0
        val totalPages = pagesStr.toIntOrNull() ?: return 0
        if (totalPages <= 0) return 0
        return ((currentPage.toFloat() / totalPages.toFloat()) * 100).toInt().coerceIn(0, 100)
    }
}
