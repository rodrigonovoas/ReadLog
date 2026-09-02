package com.rodrigonovoa.readlog.ui.booksession

import androidx.annotation.StringRes

sealed interface BookSessionEffect {
    data object NavigateBack : BookSessionEffect
    data class NavigateBackWithSnackbar(@StringRes val messageResId: Int) : BookSessionEffect
}
