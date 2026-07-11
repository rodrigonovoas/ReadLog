package com.rodrigonovoa.readlog.ui.addbook

sealed interface AddBookEffect {
    data object NavigateBack : AddBookEffect
    data object RequestCoverPicker : AddBookEffect
}
