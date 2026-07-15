package com.rodrigonovoa.readlog.ui.booksession

sealed interface BookSessionIntent {
    data object OnPlayPauseClicked : BookSessionIntent
    data object OnStopClicked : BookSessionIntent
    data object OnConfirmEndSessionClicked : BookSessionIntent
    data object OnDismissEndSessionDialogClicked : BookSessionIntent
}
