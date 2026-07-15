package com.rodrigonovoa.readlog.ui.booksession

sealed interface BookSessionEffect {
    data object NavigateBack : BookSessionEffect
}
