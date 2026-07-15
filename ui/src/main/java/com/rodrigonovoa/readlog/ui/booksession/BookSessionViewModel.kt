package com.rodrigonovoa.readlog.ui.booksession

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookSessionViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BookSessionUiState())
    val uiState: StateFlow<BookSessionUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<BookSessionEffect>()
    val effect: SharedFlow<BookSessionEffect> = _effect.asSharedFlow()

    private var timerJob: Job? = null

    fun processIntent(intent: BookSessionIntent) {
        when (intent) {
            is BookSessionIntent.OnPlayPauseClicked -> {
                if (_uiState.value.isRunning) {
                    pauseTimer()
                } else {
                    startTimer()
                }
            }
            is BookSessionIntent.OnStopClicked -> {
                pauseTimer()
                _uiState.update { it.copy(showEndSessionDialog = true) }
            }
            is BookSessionIntent.OnConfirmEndSessionClicked -> {
                _uiState.update { it.copy(showEndSessionDialog = false) }
                viewModelScope.launch { _effect.emit(BookSessionEffect.NavigateBack) }
            }
            is BookSessionIntent.OnDismissEndSessionDialogClicked -> {
                _uiState.update { it.copy(showEndSessionDialog = false) }
            }
        }
    }

    private fun startTimer() {
        _uiState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(isRunning = false) }
    }
}
