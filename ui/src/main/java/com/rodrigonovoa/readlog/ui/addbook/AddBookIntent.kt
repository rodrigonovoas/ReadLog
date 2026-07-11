package com.rodrigonovoa.readlog.ui.addbook

import android.net.Uri

sealed interface AddBookIntent {
    data class OnModeSelected(val mode: AddBookMode) : AddBookIntent
    data class OnTitleChanged(val title: String) : AddBookIntent
    data class OnAuthorChanged(val author: String) : AddBookIntent
    data class OnPagesChanged(val pages: String) : AddBookIntent
    data class OnCurrentPageChanged(val currentPage: String) : AddBookIntent
    data class OnCoverSelected(val uri: Uri?) : AddBookIntent
    data object OnAddBookClicked : AddBookIntent
    data object OnBackClicked : AddBookIntent
    data object DismissError : AddBookIntent
    data object LaunchCoverPicker : AddBookIntent
}
