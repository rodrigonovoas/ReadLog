package com.rodrigonovoa.readlog.ui.login

sealed interface LoginIntent {
    data object OnGoogleSignInClicked : LoginIntent
    data object OnContinueOfflineClicked : LoginIntent
    data object DismissError : LoginIntent
    data class OnUsernameChanged(val username: String) : LoginIntent
    data object OnUsernameConfirmClicked : LoginIntent
}
