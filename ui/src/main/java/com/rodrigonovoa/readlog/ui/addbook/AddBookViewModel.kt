package com.rodrigonovoa.readlog.ui.addbook

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.usecase.AddBookUseCase
import com.rodrigonovoa.readlog.domain.usecase.CalculateReadingProgressUseCase
import com.rodrigonovoa.readlog.domain.usecase.CapCurrentPageUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetBookByIdUseCase
import com.rodrigonovoa.readlog.domain.usecase.UpdateBookUseCase
import com.rodrigonovoa.readlog.domain.usecase.ValidateAddBookFormUseCase
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
class AddBookViewModel @Inject constructor(
    private val addBookUseCase: AddBookUseCase,
    private val validateFormUseCase: ValidateAddBookFormUseCase,
    private val capCurrentPageUseCase: CapCurrentPageUseCase,
    private val calculateProgressUseCase: CalculateReadingProgressUseCase,
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddBookUiState())
    val uiState: StateFlow<AddBookUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<AddBookEffect>()
    val effect: SharedFlow<AddBookEffect> = _effect.asSharedFlow()

    private var originalBook: Book? = null

    init {
        val bookId = savedStateHandle.get<Int>("bookId") ?: -1
        if (bookId != -1) {
            viewModelScope.launch {
                val book = getBookByIdUseCase(bookId)
                book?.let { b ->
                    originalBook = b
                    _uiState.value = AddBookUiState(
                        title = b.title,
                        author = b.author,
                        pages = b.numPages.toString(),
                        currentPage = b.currentPage.toString(),
                        progressPercentage = calculateProgressUseCase(
                            currentPageStr = b.currentPage.toString(),
                            pagesStr = b.numPages.toString(),
                        ),
                        isSubmitEnabled = validateFormUseCase(
                            title = b.title,
                            pages = b.numPages.toString(),
                            currentPage = b.currentPage.toString(),
                        ),
                        isEditMode = true,
                        bookId = b.bookId,
                    )
                }
            }
        }
    }

    fun processIntent(intent: AddBookIntent) {
        when (intent) {
            is AddBookIntent.OnModeSelected -> {
                _uiState.value = _uiState.value.copy(selectedMode = intent.mode)
            }
            is AddBookIntent.OnTitleChanged -> {
                _uiState.value = _uiState.value.copy(
                    title = intent.title,
                    isSubmitEnabled = validateFormUseCase(
                        title = intent.title,
                        pages = _uiState.value.pages,
                        currentPage = _uiState.value.currentPage,
                    ),
                )
            }
            is AddBookIntent.OnAuthorChanged -> {
                _uiState.value = _uiState.value.copy(author = intent.author)
            }
            is AddBookIntent.OnPagesChanged -> {
                val pageNumber = intent.pages.toIntOrNull()
                val cappedCurrentPage = capCurrentPageUseCase(
                    currentPageStr = _uiState.value.currentPage,
                    maxPages = pageNumber,
                )
                _uiState.value = _uiState.value.copy(
                    pages = intent.pages,
                    currentPage = cappedCurrentPage,
                    progressPercentage = calculateProgressUseCase(
                        currentPageStr = cappedCurrentPage,
                        pagesStr = intent.pages,
                    ),
                    isSubmitEnabled = validateFormUseCase(
                        title = _uiState.value.title,
                        pages = intent.pages,
                        currentPage = cappedCurrentPage,
                    ),
                )
            }
            is AddBookIntent.OnCurrentPageChanged -> {
                val cappedCurrentPage = capCurrentPageUseCase(
                    currentPageStr = intent.currentPage,
                    maxPages = _uiState.value.pages.toIntOrNull(),
                )
                _uiState.value = _uiState.value.copy(
                    currentPage = cappedCurrentPage,
                    progressPercentage = calculateProgressUseCase(
                        currentPageStr = cappedCurrentPage,
                        pagesStr = _uiState.value.pages,
                    ),
                    isSubmitEnabled = validateFormUseCase(
                        title = _uiState.value.title,
                        pages = _uiState.value.pages,
                        currentPage = cappedCurrentPage,
                    ),
                )
            }
            is AddBookIntent.OnCoverSelected -> {
                _uiState.value = _uiState.value.copy(coverUri = intent.uri)
            }
            is AddBookIntent.OnAddBookClicked -> {
                submitBook()
            }
            is AddBookIntent.OnBackClicked -> {
                val state = _uiState.value
                if (state.isEditMode) {
                    viewModelScope.launch { _effect.emit(AddBookEffect.NavigateBack) }
                } else {
                    val hasData = state.title.isNotEmpty() ||
                        state.author.isNotEmpty() ||
                        state.pages.isNotEmpty() ||
                        state.currentPage.isNotEmpty() ||
                        state.coverUri != null
                    if (hasData) {
                        _uiState.value = state.copy(showExitConfirmation = true)
                    } else {
                        viewModelScope.launch { _effect.emit(AddBookEffect.NavigateBack) }
                    }
                }
            }
            is AddBookIntent.OnConfirmExitClicked -> {
                _uiState.value = _uiState.value.copy(showExitConfirmation = false)
                viewModelScope.launch { _effect.emit(AddBookEffect.NavigateBack) }
            }
            is AddBookIntent.OnDismissExitClicked -> {
                _uiState.value = _uiState.value.copy(showExitConfirmation = false)
            }
            is AddBookIntent.DismissError -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
            is AddBookIntent.LaunchCoverPicker -> {
                viewModelScope.launch { _effect.emit(AddBookEffect.RequestCoverPicker) }
            }
        }
    }

    private fun submitBook() {
        val state = _uiState.value
        val numPages = state.pages.toIntOrNull() ?: return
        val currentPage = state.currentPage.toIntOrNull() ?: 0

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = if (state.isEditMode && originalBook != null) {
                updateBookUseCase(
                    original = originalBook!!,
                    title = state.title,
                    author = state.author,
                    numPages = numPages,
                    currentPage = currentPage,
                )
            } else {
                addBookUseCase(
                    title = state.title,
                    author = state.author,
                    numPages = numPages,
                    currentPage = currentPage,
                )
            }

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _effect.emit(AddBookEffect.NavigateBack)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to save book",
                )
            }
        }
    }
}
