package com.rodrigonovoa.readlog.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigonovoa.readlog.domain.usecase.ContinueOfflineUseCase
import com.rodrigonovoa.readlog.domain.usecase.SignInWithGoogleUseCase
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
    private val continueOfflineUseCase: ContinueOfflineUseCase
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
                    _effect.emit(LoginEffect.LaunchGoogleSignIn)
                }
            }
            is LoginIntent.OnGoogleTokenReceived -> {
                viewModelScope.launch {
                    val result = signInWithGoogleUseCase(intent.token)
                    if (result.isSuccess) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _effect.emit(LoginEffect.NavigateToCollection)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Authentication failed"
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
            is LoginIntent.OnGoogleSignInFailed -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = intent.message
                )
            }
            is LoginIntent.DismissError -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
        }
    }
}
