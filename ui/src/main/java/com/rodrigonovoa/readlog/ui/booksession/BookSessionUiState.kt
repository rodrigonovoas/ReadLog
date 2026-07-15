package com.rodrigonovoa.readlog.ui.booksession

data class BookSessionUiState(
    val elapsedSeconds: Long = 0L,
    val isRunning: Boolean = false,
    val showEndSessionDialog: Boolean = false,
)
