package com.rodrigonovoa.readlog.ui.login

import com.rodrigonovoa.readlog.ui.common.UsernameSetupState

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val usernameSetup: UsernameSetupState? = null,
)
