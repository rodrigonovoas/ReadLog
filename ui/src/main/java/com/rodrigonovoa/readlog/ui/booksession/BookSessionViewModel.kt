package com.rodrigonovoa.readlog.ui.booksession

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigonovoa.readlog.ui.R
import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.usecase.AddAnnotationUseCase
import com.rodrigonovoa.readlog.domain.usecase.AddSessionUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetBookByIdUseCase
import com.rodrigonovoa.readlog.domain.usecase.RefreshUserProfileIfOnlineUseCase
import com.rodrigonovoa.readlog.domain.usecase.UpdateBookUseCase
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
    private val updateBookUseCase: UpdateBookUseCase,
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
    private var currentBook: Book? = null

    init {
        loadBook()
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
                val hasPendingPages = _uiState.value.pendingPages > 0
                if (hasStartedTimer || hasAnnotations || hasPendingPages) {
                    resumeTimerOnDismiss = _uiState.value.isRunning
                    pauseTimer()
                    dialogTriggeredByBack = true
                    _uiState.update { it.copy(showEndSessionDialog = true) }
                } else {
                    viewModelScope.launch { _effect.emit(BookSessionEffect.NavigateBack) }
                }
            }
            is BookSessionIntent.OnDiscardSessionClicked -> {
                _uiState.update { it.copy(showEndSessionDialog = false) }
                viewModelScope.launch { _effect.emit(BookSessionEffect.NavigateBack) }
            }
            is BookSessionIntent.OnSaveAndFinishSessionClicked -> {
                _uiState.update { it.copy(showEndSessionDialog = false) }
                saveSession()
            }
            is BookSessionIntent.OnDismissEndSessionDialogClicked -> {
                _uiState.update { it.copy(showEndSessionDialog = false) }
                if (dialogTriggeredByBack && resumeTimerOnDismiss) {
                    startTimer()
                }
            }
            is BookSessionIntent.OnRetryLoadClicked -> loadBook()
            is BookSessionIntent.OnOpenAnnotationDialogClicked -> {
                _uiState.update { it.copy(showAnnotationDialog = true) }
            }
            is BookSessionIntent.OnDismissAnnotationDialogClicked -> {
                _uiState.update { it.copy(showAnnotationDialog = false) }
            }
            is BookSessionIntent.OnAnnotationTextChanged -> {
                val lineCount = intent.text.count { it == '\n' } + 1
                if (lineCount <= MAX_ANNOTATION_LINES) {
                    _uiState.update { it.copy(annotationText = intent.text) }
                }
            }
            is BookSessionIntent.OnModeSelected -> {
                val currentMode = _uiState.value.selectedMode
                if (currentMode == intent.mode) return
                when (intent.mode) {
                    BookSessionMode.Manual -> {
                        pauseTimer()
                        _uiState.update {
                            it.copy(
                                selectedMode = BookSessionMode.Manual,
                                manualHours = (it.elapsedSeconds / 3600).toInt(),
                                manualMinutes = ((it.elapsedSeconds % 3600) / 60).toInt(),
                                manualDateMillis = it.sessionDate,
                            )
                        }
                    }
                    BookSessionMode.Timer -> {
                        val state = _uiState.value
                        _uiState.update {
                            it.copy(
                                selectedMode = BookSessionMode.Timer,
                                elapsedSeconds = state.manualHours * 3600L + state.manualMinutes * 60L,
                                sessionDate = state.manualDateMillis,
                                sessionStatus = if (state.manualHours > 0 || state.manualMinutes > 0) {
                                    BookSessionStatus.Paused
                                } else {
                                    BookSessionStatus.NotStarted
                                },
                            )
                        }
                    }
                }
            }
            is BookSessionIntent.OnManualHoursChanged -> {
                val digitsOnly = intent.hours.filter { it.isDigit() }.take(2)
                val parsed = digitsOnly.toIntOrNull() ?: 0
                val capped = if (parsed > 23) 23 else parsed
                _uiState.update { it.copy(manualHours = capped) }
            }
            is BookSessionIntent.OnManualMinutesChanged -> {
                val digitsOnly = intent.minutes.filter { it.isDigit() }.take(2)
                val parsed = digitsOnly.toIntOrNull() ?: 0
                val capped = if (parsed > 59) 59 else parsed
                _uiState.update { it.copy(manualMinutes = capped) }
            }
            is BookSessionIntent.OnManualDateChanged -> {
                _uiState.update {
                    it.copy(
                        manualDateMillis = intent.dateMillis,
                        sessionDate = intent.dateMillis,
                    )
                }
            }
            is BookSessionIntent.OnSaveManualTimeClicked -> {
                val state = _uiState.value
                hasStartedTimer = true
                _uiState.update {
                    it.copy(
                        elapsedSeconds = state.manualHours * 3600L + state.manualMinutes * 60L,
                        sessionDate = state.manualDateMillis,
                    )
                }
                saveSession()
            }
            is BookSessionIntent.OnOpenPageDialogClicked -> {
                _uiState.update {
                    it.copy(
                        showPageDialog = true,
                        pageDialogInput = it.pendingPages.toString(),
                    )
                }
            }
            is BookSessionIntent.OnDismissPageDialogClicked -> {
                _uiState.update { it.copy(showPageDialog = false) }
            }
            is BookSessionIntent.OnPageDialogInputChanged -> {
                val digitsOnly = intent.input.filter { it.isDigit() }
                _uiState.update { it.copy(pageDialogInput = digitsOnly) }
            }
            is BookSessionIntent.OnConfirmPageDialogClicked -> {
                val pagesToAdd = _uiState.value.pageDialogInput.toIntOrNull() ?: 0
                _uiState.update {
                    it.copy(
                        pendingPages = pagesToAdd,
                        showPageDialog = false,
                        pageDialogInput = "",
                    )
                }
            }
        }
    }

    private fun startTimer() {
        hasStartedTimer = true
        _uiState.update {
            it.copy(isRunning = true, sessionStatus = BookSessionStatus.Reading)
        }
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
        _uiState.update {
            it.copy(
                isRunning = false,
                sessionStatus = if (it.elapsedSeconds > 0L) {
                    BookSessionStatus.Paused
                } else {
                    BookSessionStatus.NotStarted
                },
            )
        }
    }

    private fun saveSession() {
        viewModelScope.launch {
            val state = _uiState.value
            val pagesUpdated = applyPendingPages(state)
            if (state.elapsedSeconds == 0L) {
                if (pagesUpdated) {
                    _effect.emit(BookSessionEffect.NavigateBackWithSnackbar(R.string.book_session_saved_message))
                } else {
                    _effect.emit(BookSessionEffect.NavigateBack)
                }
                return@launch
            }
            val result = addSessionUseCase(bookId, state.elapsedSeconds, state.sessionDate)
            val session = result.getOrNull()
            val annotationText = state.annotationText.trim()
            if (session != null) {
                if (annotationText.isNotEmpty()) {
                    addAnnotationUseCase(session.sessionId, annotationText)
                }
                refreshUserProfileIfOnlineUseCase()
            }
            _effect.emit(BookSessionEffect.NavigateBackWithSnackbar(R.string.book_session_saved_message))
        }
    }

    private fun loadBook() {
        if (bookId == -1) {
            _uiState.update { it.copy(isLoading = false, loadError = true) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = false) }
            val book = getBookByIdUseCase(bookId)
            currentBook = book
            _uiState.update {
                it.copy(
                    bookTitle = book?.title ?: "",
                    currentPage = book?.currentPage ?: 0,
                    totalPages = book?.numPages ?: 0,
                    isLoading = false,
                    loadError = book == null,
                )
            }
        }
    }

    private suspend fun applyPendingPages(state: BookSessionUiState): Boolean {
        val book = currentBook ?: return false
        val pagesToAdd = state.pendingPages
        if (pagesToAdd <= 0) return false
        val newPage = (state.currentPage + pagesToAdd).coerceAtMost(state.totalPages)
        if (newPage == state.currentPage) return false
        val result = updateBookUseCase(
            original = book,
            title = book.title,
            author = book.author,
            numPages = book.numPages,
            currentPage = newPage,
            state = book.state,
        )
        currentBook = book.copy(currentPage = newPage)
        return result.isSuccess
    }
}
