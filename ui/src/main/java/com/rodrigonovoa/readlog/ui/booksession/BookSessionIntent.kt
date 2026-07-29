package com.rodrigonovoa.readlog.ui.booksession

sealed interface BookSessionIntent {
    data object OnPlayPauseClicked : BookSessionIntent
    data object OnStopClicked : BookSessionIntent
    data object OnBackClicked : BookSessionIntent
    data object OnConfirmEndSessionClicked : BookSessionIntent
    data object OnDismissEndSessionDialogClicked : BookSessionIntent
    data object OnOpenAnnotationDialogClicked : BookSessionIntent
    data object OnDismissAnnotationDialogClicked : BookSessionIntent
    data class OnAnnotationTextChanged(val text: String) : BookSessionIntent
    data class OnConfirmManualTimeClicked(
        val hours: Int,
        val minutes: Int,
        val dateMillis: Long,
        val annotation: String,
    ) : BookSessionIntent
}
