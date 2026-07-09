package com.rodrigonovoa.readlog.ui.login

sealed interface LoginIntent {
    data object OnGoogleSignInClicked : LoginIntent
    data class OnGoogleTokenReceived(val token: String) : LoginIntent
    data class OnGoogleSignInFailed(val message: String?) : LoginIntent
    data object OnContinueOfflineClicked : LoginIntent
    data object DismissError : LoginIntent
}
