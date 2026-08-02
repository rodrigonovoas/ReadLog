package com.rodrigonovoa.readlog.ui.bookcollection

sealed interface BookCollectionEffect {
    data object NavigateToLogin : BookCollectionEffect
}
