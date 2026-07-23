package com.rodrigonovoa.readlog.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigonovoa.readlog.domain.auth.AuthLauncher
import com.rodrigonovoa.readlog.domain.usecase.ClaimUsernameResult
import com.rodrigonovoa.readlog.domain.usecase.ClaimUsernameUseCase
import com.rodrigonovoa.readlog.domain.usecase.ContinueOfflineUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetCurrentUserUseCase
import com.rodrigonovoa.readlog.domain.usecase.IsOnlineUseCase
import com.rodrigonovoa.readlog.domain.usecase.RequireUsernameSetupUseCase
import com.rodrigonovoa.readlog.domain.usecase.SignInWithGoogleUseCase
import com.rodrigonovoa.readlog.ui.R
import com.rodrigonovoa.readlog.ui.common.UsernameSetupState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val continueOfflineUseCase: ContinueOfflineUseCase,
    private val authLauncher: AuthLauncher,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val isOnlineUseCase: IsOnlineUseCase,
    private val requireUsernameSetupUseCase: RequireUsernameSetupUseCase,
    private val claimUsernameUseCase: ClaimUsernameUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect: SharedFlow<LoginEffect> = _effect.asSharedFlow()

    fun processIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.OnGoogleSignInClicked -> {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                viewModelScope.launch {
                    val tokenResult = authLauncher.launchGoogleSignIn()
                    if (tokenResult.isSuccess) {
                        val signInResult = signInWithGoogleUseCase(tokenResult.getOrThrow())
                        if (signInResult.isSuccess) {
                            maybeShowUsernameSetup()
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = signInResult.exceptionOrNull()?.message ?: "Authentication failed"
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = tokenResult.exceptionOrNull()?.message ?: "Google sign-in failed"
                        )
                    }
                }
            }
            is LoginIntent.OnContinueOfflineClicked -> {
                viewModelScope.launch {
                    continueOfflineUseCase()
                    _effect.emit(LoginEffect.NavigateToCollection)
                }
            }
            is LoginIntent.DismissError -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
            is LoginIntent.OnUsernameChanged -> {
                val current = _uiState.value.usernameSetup ?: return
                _uiState.value = _uiState.value.copy(
                    usernameSetup = current.copy(username = intent.username, errorMessageRes = null),
                )
            }
            is LoginIntent.OnUsernameConfirmClicked -> {
                onUsernameConfirmClicked()
            }
        }
    }

    private suspend fun maybeShowUsernameSetup() {
        val currentUser = getCurrentUserUseCase()
        if (currentUser == null || !isOnlineUseCase()) {
            _uiState.value = _uiState.value.copy(isLoading = false)
            _effect.emit(LoginEffect.NavigateToCollection)
            return
        }

        val suggestion = requireUsernameSetupUseCase(currentUser.uid, currentUser.email, currentUser.displayName)
        if (suggestion == null) {
            _uiState.value = _uiState.value.copy(isLoading = false)
            _effect.emit(LoginEffect.NavigateToCollection)
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            usernameSetup = UsernameSetupState(username = suggestion),
        )
    }

    private fun onUsernameConfirmClicked() {
        val setupState = _uiState.value.usernameSetup ?: return
        val currentUser = getCurrentUserUseCase() ?: return

        _uiState.value = _uiState.value.copy(
            usernameSetup = setupState.copy(isChecking = true, errorMessageRes = null),
        )
        viewModelScope.launch {
            when (claimUsernameUseCase(currentUser.uid, setupState.username)) {
                is ClaimUsernameResult.Success -> {
                    _uiState.value = _uiState.value.copy(usernameSetup = null)
                    _effect.emit(LoginEffect.NavigateToCollection)
                }
                ClaimUsernameResult.InvalidFormat -> {
                    updateUsernameSetupError(R.string.username_setup_error_invalid)
                }
                ClaimUsernameResult.AlreadyTaken -> {
                    updateUsernameSetupError(R.string.username_setup_error_taken)
                }
                is ClaimUsernameResult.Error -> {
                    updateUsernameSetupError(R.string.username_setup_error_generic)
                }
            }
        }
    }

    private fun updateUsernameSetupError(errorRes: Int) {
        val current = _uiState.value.usernameSetup ?: return
        _uiState.value = _uiState.value.copy(
            usernameSetup = current.copy(isChecking = false, errorMessageRes = errorRes),
        )
    }
}
