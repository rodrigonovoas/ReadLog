package com.rodrigonovoa.readlog.ui.login

sealed interface LoginEffect {
    data object NavigateToCollection : LoginEffect
}
