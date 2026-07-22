package com.rodrigonovoa.readlog.ui.booksession

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigonovoa.readlog.domain.usecase.AddAnnotationUseCase
import com.rodrigonovoa.readlog.domain.usecase.AddSessionUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetBookByIdUseCase
import com.rodrigonovoa.readlog.domain.usecase.RefreshUserProfileIfOnlineUseCase
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

private const val MAX_ANNOTATION_LINES = 3

@HiltViewModel
class BookSessionViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val addSessionUseCase: AddSessionUseCase,
    private val addAnnotationUseCase: AddAnnotationUseCase,
    private val refreshUserProfileIfOnlineUseCase: RefreshUserProfileIfOnlineUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val bookId: Int = savedStateHandle.get<Int>("bookId") ?: -1

    private val _uiState = MutableStateFlow(BookSessionUiState())
    val uiState: StateFlow<BookSessionUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<BookSessionEffect>()
    val effect: SharedFlow<BookSessionEffect> = _effect.asSharedFlow()

    private var timerJob: Job? = null
    private var hasStartedTimer = false
    private var dialogTriggeredByBack = false
    private var resumeTimerOnDismiss = false

    init {
        if (bookId != -1) {
            viewModelScope.launch {
                val book = getBookByIdUseCase(bookId)
                _uiState.update { it.copy(bookTitle = book?.title ?: "") }
            }
        }
    }

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
                dialogTriggeredByBack = false
                _uiState.update { it.copy(showEndSessionDialog = true) }
            }
            is BookSessionIntent.OnBackClicked -> {
                val hasAnnotations = _uiState.value.annotationText.isNotBlank()
                if (hasStartedTimer || hasAnnotations) {
                    resumeTimerOnDismiss = _uiState.value.isRunning
                    pauseTimer()
                    dialogTriggeredByBack = true
                    _uiState.update { it.copy(showEndSessionDialog = true) }
                } else {
                    viewModelScope.launch { _effect.emit(BookSessionEffect.NavigateBack) }
                }
            }
            is BookSessionIntent.OnConfirmEndSessionClicked -> {
                _uiState.update { it.copy(showEndSessionDialog = false) }
                if (dialogTriggeredByBack) {
                    viewModelScope.launch { _effect.emit(BookSessionEffect.NavigateBack) }
                } else {
                    saveSession()
                }
            }
            is BookSessionIntent.OnDismissEndSessionDialogClicked -> {
                _uiState.update { it.copy(showEndSessionDialog = false) }
                if (dialogTriggeredByBack && resumeTimerOnDismiss) {
                    startTimer()
                }
            }
            is BookSessionIntent.OnAnnotationTextChanged -> {
                val lineCount = intent.text.count { it == '\n' } + 1
                if (lineCount <= MAX_ANNOTATION_LINES) {
                    _uiState.update { it.copy(annotationText = intent.text) }
                }
            }
        }
    }

    private fun startTimer() {
        hasStartedTimer = true
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

    private fun saveSession() {
        viewModelScope.launch {
            val state = _uiState.value
            val result = addSessionUseCase(bookId, state.elapsedSeconds)
            val session = result.getOrNull()
            val annotationText = state.annotationText.trim()
            if (session != null) {
                if (annotationText.isNotEmpty()) {
                    addAnnotationUseCase(session.sessionId, annotationText)
                }
                refreshUserProfileIfOnlineUseCase()
            }
            _effect.emit(BookSessionEffect.NavigateBack)
        }
    }
}
