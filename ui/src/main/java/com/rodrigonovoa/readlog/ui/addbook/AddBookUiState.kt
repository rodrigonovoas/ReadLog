package com.rodrigonovoa.readlog.ui.addbook

import com.rodrigonovoa.readlog.domain.model.BookState

data class AddBookUiState(
    val selectedMode: AddBookMode = AddBookMode.Manual,
    val title: String = "",
    val author: String = "",
    val pages: String = "",
    val currentPage: String = "",
    val state: BookState = BookState.IN_PROGRESS,
    val coverUrl: String = "",
    val isLoading: Boolean = false,
    val isSubmitEnabled: Boolean = false,
    val progressPercentage: Int = 0,
    val errorMessage: String? = null,
    val showExitConfirmation: Boolean = false,
    val isEditMode: Boolean = false,
    val bookId: Int? = null,
    val isScanning: Boolean = false,
    val scanError: ScanError? = null,
    val hasCameraPermission: Boolean = false,
    val manualIsbn: String = "",
    val isManualIsbnSearchEnabled: Boolean = false,
)
