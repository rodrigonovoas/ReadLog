package com.rodrigonovoa.readlog.ui.common

data class UsernameSetupState(
    val username: String = "",
    val isChecking: Boolean = false,
    val errorMessageRes: Int? = null,
)
