package com.rodrigonovoa.readlog.ui.booksession

data class BookSessionUiState(
    val bookTitle: String = "",
    val elapsedSeconds: Long = 0L,
    val isRunning: Boolean = false,
    val showEndSessionDialog: Boolean = false,
    val annotationText: String = "",
)
