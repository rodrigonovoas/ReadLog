package com.rodrigonovoa.readlog.domain.usecase

import javax.inject.Inject

class CapCurrentPageUseCase @Inject constructor() {
    operator fun invoke(
        currentPageStr: String,
        maxPages: Int?,
    ): String {
        if (currentPageStr.isEmpty()) return currentPageStr
        val currentPageNumber = currentPageStr.toIntOrNull() ?: return currentPageStr
        if (maxPages == null) return currentPageStr
        return if (currentPageNumber > maxPages) maxPages.toString() else currentPageStr
    }
}
