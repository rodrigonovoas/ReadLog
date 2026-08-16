package com.rodrigonovoa.readlog.ui.bookcollection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigonovoa.readlog.domain.model.BookFilters
import com.rodrigonovoa.readlog.domain.model.User
import com.rodrigonovoa.readlog.domain.usecase.ClaimUsernameResult
import com.rodrigonovoa.readlog.domain.usecase.ClaimUsernameUseCase
import com.rodrigonovoa.readlog.domain.usecase.DeleteBookUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetBooksUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetCurrentUserUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetTimeOfDayUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetUserDisplayNameUseCase
import com.rodrigonovoa.readlog.domain.usecase.IsOnlineUseCase
import com.rodrigonovoa.readlog.domain.usecase.RefreshUserProfileIfOnlineUseCase
import com.rodrigonovoa.readlog.domain.usecase.ClearLocalDataUseCase
import com.rodrigonovoa.readlog.domain.usecase.RequireUsernameSetupUseCase
import com.rodrigonovoa.readlog.domain.usecase.SignOutUseCase
import com.rodrigonovoa.readlog.domain.usecase.SyncUserDataUseCase
import com.rodrigonovoa.readlog.domain.usecase.TimeOfDay
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
class BookCollectionViewModel @Inject constructor(
    private val getBooksUseCase: GetBooksUseCase,
    private val getUserDisplayNameUseCase: GetUserDisplayNameUseCase,
    private val getTimeOfDayUseCase: GetTimeOfDayUseCase,
    private val deleteBookUseCase: DeleteBookUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val syncUserDataUseCase: SyncUserDataUseCase,
    private val refreshUserProfileIfOnlineUseCase: RefreshUserProfileIfOnlineUseCase,
    private val isOnlineUseCase: IsOnlineUseCase,
    private val requireUsernameSetupUseCase: RequireUsernameSetupUseCase,
    private val claimUsernameUseCase: ClaimUsernameUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val clearLocalDataUseCase: ClearLocalDataUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookCollectionUiState())
    val uiState: StateFlow<BookCollectionUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<BookCollectionEffect>()
    val effect: SharedFlow<BookCollectionEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            getBooksUseCase(_uiState.value.bookFilters).collect { bookList ->
                _uiState.value = _uiState.value.copy(books = bookList)
            }
        }
        buildGreeting()
        refreshUserDataIfOnline()
    }

    fun onFilterClick() {
        _uiState.value = _uiState.value.copy(showFilterDialog = true)
    }

    fun onFilterAccepted(filters: BookFilters) {
        _uiState.value = _uiState.value.copy(
            bookFilters = filters,
            showFilterDialog = false,
        )
        viewModelScope.launch {
            getBooksUseCase(filters).collect { bookList ->
                _uiState.value = _uiState.value.copy(books = bookList)
            }
        }
    }

    fun dismissFilterDialog() {
        _uiState.value = _uiState.value.copy(showFilterDialog = false)
    }

    fun clearFilters() {
        onFilterAccepted(BookFilters())
    }

    private fun refreshUserDataIfOnline() {
        val currentUser = getCurrentUserUseCase() ?: return
        if (!isOnlineUseCase()) return
        _uiState.value = _uiState.value.copy(isSyncing = true)
        viewModelScope.launch {
            runCatching { syncUserDataUseCase(currentUser.uid) }
            refreshUserProfileIfOnlineUseCase()
            if (!currentUser.isAnonymous) {
                checkUsernameSetup(currentUser)
            }
            _uiState.value = _uiState.value.copy(isSyncing = false)
        }
    }

    private suspend fun checkUsernameSetup(currentUser: User) {
        val suggestion = requireUsernameSetupUseCase(currentUser.uid, currentUser.email, currentUser.displayName)
        if (suggestion != null) {
            _uiState.value = _uiState.value.copy(usernameSetup = UsernameSetupState(username = suggestion))
        }
    }

    fun onUsernameChanged(username: String) {
        val current = _uiState.value.usernameSetup ?: return
        _uiState.value = _uiState.value.copy(
            usernameSetup = current.copy(username = username, errorMessageRes = null),
        )
    }

    fun onUsernameConfirmClicked() {
        val setupState = _uiState.value.usernameSetup ?: return
        val currentUser = getCurrentUserUseCase() ?: return

        _uiState.value = _uiState.value.copy(
            usernameSetup = setupState.copy(isChecking = true, errorMessageRes = null),
        )
        viewModelScope.launch {
            when (claimUsernameUseCase(currentUser.uid, setupState.username)) {
                is ClaimUsernameResult.Success -> {
                    _uiState.value = _uiState.value.copy(usernameSetup = null)
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

    private fun buildGreeting() {
        val name = getUserDisplayNameUseCase().orEmpty()
        val currentUser = getCurrentUserUseCase()
        val greetingResId = when (getTimeOfDayUseCase()) {
            TimeOfDay.MORNING -> R.string.book_collection_greeting_morning
            TimeOfDay.AFTERNOON -> R.string.book_collection_greeting_afternoon
            TimeOfDay.EVENING -> R.string.book_collection_greeting_evening
        }
        _uiState.value = _uiState.value.copy(
            greetingResId = greetingResId,
            userName = name,
            canLike = currentUser != null && !currentUser.isAnonymous,
        )
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

    fun onLogoutClicked() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = true)
    }

    fun dismissLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = false)
    }

    fun confirmLogout() {
        viewModelScope.launch {
            dismissLogoutDialog()
            clearLocalDataUseCase()
            signOutUseCase()
            _effect.emit(BookCollectionEffect.NavigateToLogin)
        }
    }
}
