package com.rodrigonovoa.readlog.ui.addbook

import android.net.Uri

data class AddBookUiState(
    val selectedMode: AddBookMode = AddBookMode.Manual,
    val title: String = "",
    val author: String = "",
    val pages: String = "",
    val currentPage: String = "",
    val coverUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSubmitEnabled: Boolean = false,
    val progressPercentage: Int = 0,
    val errorMessage: String? = null,
    val showExitConfirmation: Boolean = false,
)
