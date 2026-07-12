package com.rodrigonovoa.readlog.ui.bookcollection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigonovoa.readlog.domain.usecase.GetBooksUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetCurrentUserUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetTimeOfDayUseCase
import com.rodrigonovoa.readlog.domain.usecase.TimeOfDay
import com.rodrigonovoa.readlog.domain.usecase.InsertMockBooksUseCase
import com.rodrigonovoa.readlog.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookCollectionViewModel @Inject constructor(
    private val getBooksUseCase: GetBooksUseCase,
    private val insertMockBooksUseCase: InsertMockBooksUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getTimeOfDayUseCase: GetTimeOfDayUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookCollectionUiState())
    val uiState: StateFlow<BookCollectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            insertMockBooksUseCase()
            getBooksUseCase().collect { bookList ->
                _uiState.value = _uiState.value.copy(books = bookList)
            }
        }
        buildGreeting()
    }

    private fun buildGreeting() {
        val user = getCurrentUserUseCase()
        val rawName = user?.displayName?.ifBlank { null }
        val name = rawName?.split(" ")?.firstOrNull() ?: "reader"
        val greetingResId = when (getTimeOfDayUseCase()) {
            TimeOfDay.MORNING -> R.string.book_collection_greeting_morning
            TimeOfDay.AFTERNOON -> R.string.book_collection_greeting_afternoon
            TimeOfDay.EVENING -> R.string.book_collection_greeting_evening
        }
        _uiState.value = _uiState.value.copy(
            greetingResId = greetingResId,
            userName = name,
        )
    }

    fun selectBook(bookId: Int) {
        if (_uiState.value.selectedBookId == null) {
            _uiState.value = _uiState.value.copy(selectedBookId = bookId)
        }
    }

    fun dismissPopup() {
        _uiState.value = _uiState.value.copy(selectedBookId = null)
    }

    fun onEditClick() {
        dismissPopup()
    }

    fun onDeleteClick() {
        dismissPopup()
    }
}
