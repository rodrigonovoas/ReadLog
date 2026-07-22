package com.rodrigonovoa.readlog.ui.bookcollection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigonovoa.readlog.domain.usecase.DeleteBookUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetBooksUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetCurrentUserUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetTimeOfDayUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetUserDisplayNameUseCase
import com.rodrigonovoa.readlog.domain.usecase.IsOnlineUseCase
import com.rodrigonovoa.readlog.domain.usecase.RefreshUserProfileIfOnlineUseCase
import com.rodrigonovoa.readlog.domain.usecase.SyncUserDataUseCase
import com.rodrigonovoa.readlog.domain.usecase.TimeOfDay
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
    private val getUserDisplayNameUseCase: GetUserDisplayNameUseCase,
    private val getTimeOfDayUseCase: GetTimeOfDayUseCase,
    private val deleteBookUseCase: DeleteBookUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val syncUserDataUseCase: SyncUserDataUseCase,
    private val refreshUserProfileIfOnlineUseCase: RefreshUserProfileIfOnlineUseCase,
    private val isOnlineUseCase: IsOnlineUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookCollectionUiState())
    val uiState: StateFlow<BookCollectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getBooksUseCase().collect { bookList ->
                _uiState.value = _uiState.value.copy(books = bookList)
            }
        }
        buildGreeting()
        refreshUserDataIfOnline()
    }

    private fun refreshUserDataIfOnline() {
        val currentUser = getCurrentUserUseCase() ?: return
        if (!isOnlineUseCase()) return
        viewModelScope.launch {
            runCatching { syncUserDataUseCase(currentUser.uid) }
            refreshUserProfileIfOnlineUseCase()
        }
    }

    private fun buildGreeting() {
        val name = getUserDisplayNameUseCase()
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

    fun onEditIconClick(bookId: Int) {
        openDialog(bookId, BookDialogType.EDIT)
    }

    fun onDeleteIconClick(bookId: Int) {
        openDialog(bookId, BookDialogType.DELETE)
    }

    private fun openDialog(bookId: Int, type: BookDialogType) {
        val book = _uiState.value.books.find { it.bookId == bookId } ?: return
        _uiState.value = _uiState.value.copy(
            activeDialog = BookDialogState(bookId = bookId, bookTitle = book.title, type = type),
        )
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(activeDialog = null)
    }

    fun confirmDelete() {
        val dialog = _uiState.value.activeDialog ?: return
        val selectedBook = _uiState.value.books.find { it.bookId == dialog.bookId }
        dismissDialog()
        if (selectedBook != null) {
            viewModelScope.launch {
                deleteBookUseCase(selectedBook)
                refreshUserProfileIfOnlineUseCase()
            }
        }
    }
}
