package com.rodrigonovoa.readlog.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigonovoa.readlog.domain.auth.AuthLauncher
import com.rodrigonovoa.readlog.domain.usecase.ContinueOfflineUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetCurrentUserUseCase
import com.rodrigonovoa.readlog.domain.usecase.SignInWithGoogleUseCase
import com.rodrigonovoa.readlog.domain.usecase.SyncUserDataUseCase
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
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val syncUserDataUseCase: SyncUserDataUseCase,
    private val authLauncher: AuthLauncher,
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
                            getCurrentUserUseCase()?.uid?.let { uid ->
                                runCatching { syncUserDataUseCase(uid) }
                            }
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            _effect.emit(LoginEffect.NavigateToCollection)
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
        }
    }
}
