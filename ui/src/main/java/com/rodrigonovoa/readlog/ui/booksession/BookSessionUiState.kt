package com.rodrigonovoa.readlog.ui.booksession

data class BookSessionUiState(
    val bookTitle: String = "",
    val elapsedSeconds: Long = 0L,
    val isRunning: Boolean = false,
    val showEndSessionDialog: Boolean = false,
    val showAnnotationDialog: Boolean = false,
    val annotationText: String = "",
    val sessionDate: Long = System.currentTimeMillis(),
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val pendingPages: Int = 0,
    val showPageDialog: Boolean = false,
    val pageDialogInput: String = "",
)
