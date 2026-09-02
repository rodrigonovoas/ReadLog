package com.rodrigonovoa.readlog.ui.booksession

sealed interface BookSessionIntent {
    data object OnPlayPauseClicked : BookSessionIntent
    data object OnStopClicked : BookSessionIntent
    data object OnBackClicked : BookSessionIntent
    data object OnDiscardSessionClicked : BookSessionIntent
    data object OnSaveAndFinishSessionClicked : BookSessionIntent
    data object OnDismissEndSessionDialogClicked : BookSessionIntent
    data object OnRetryLoadClicked : BookSessionIntent
    data object OnOpenAnnotationDialogClicked : BookSessionIntent
    data object OnDismissAnnotationDialogClicked : BookSessionIntent
    data class OnAnnotationTextChanged(val text: String) : BookSessionIntent
    data class OnModeSelected(val mode: BookSessionMode) : BookSessionIntent
    data class OnManualHoursChanged(val hours: String) : BookSessionIntent
    data class OnManualMinutesChanged(val minutes: String) : BookSessionIntent
    data class OnManualDateChanged(val dateMillis: Long) : BookSessionIntent
    data object OnSaveManualTimeClicked : BookSessionIntent
    data object OnOpenPageDialogClicked : BookSessionIntent
    data object OnDismissPageDialogClicked : BookSessionIntent
    data class OnPageDialogInputChanged(val input: String) : BookSessionIntent
    data object OnConfirmPageDialogClicked : BookSessionIntent
}
