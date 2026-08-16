package com.rodrigonovoa.readlog.ui.bookcollection

import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.model.BookFilters
import com.rodrigonovoa.readlog.ui.common.UsernameSetupState

data class BookCollectionUiState(
    val books: List<Book> = emptyList(),
    val greetingResId: Int = 0,
    val userName: String = "",
    val activeDialog: BookDialogState? = null,
    val usernameSetup: UsernameSetupState? = null,
    val canLike: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val isSyncing: Boolean = false,
    val bookFilters: BookFilters = BookFilters(),
    val showFilterDialog: Boolean = false,
)

data class BookDialogState(
    val bookId: Int,
    val bookTitle: String,
    val type: BookDialogType,
)

enum class BookDialogType {
    EDIT,
    DELETE,
}
