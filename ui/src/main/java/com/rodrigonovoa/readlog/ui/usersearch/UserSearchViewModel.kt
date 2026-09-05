package com.rodrigonovoa.readlog.ui.usersearch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigonovoa.readlog.domain.usecase.GetLikedProfilesUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetCurrentUserUseCase
import com.rodrigonovoa.readlog.domain.usecase.SearchUsersUseCase
import com.rodrigonovoa.readlog.domain.usecase.ToggleUserLikeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SEARCH_DEBOUNCE_MILLIS = 300L

@OptIn(FlowPreview::class)
@HiltViewModel
class UserSearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val getLikedProfilesUseCase: GetLikedProfilesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val toggleUserLikeUseCase: ToggleUserLikeUseCase,
) : ViewModel() {

    private val mode = UserSearchMode.valueOf(
        savedStateHandle.get<String>(MODE_ARG) ?: UserSearchMode.SEARCH.name,
    )

    private val _uiState = MutableStateFlow(UserSearchUiState(mode = mode))
    val uiState: StateFlow<UserSearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        if (mode == UserSearchMode.LIKES) {
            loadLikedProfiles()
        } else {
            observeSearchQueries()
        }
    }

    private fun observeSearchQueries() {
        viewModelScope.launch {
            queryFlow
                .debounce(SEARCH_DEBOUNCE_MILLIS)
                .distinctUntilChanged()
                .collectLatest { query ->
                    _uiState.update { it.copy(isLoading = query.isNotBlank(), hasError = false) }

                    searchUsersUseCase(query).fold(
                        onSuccess = { results ->
                            _uiState.update {
                                it.copy(
                                    results = results.map { result -> UserSearchResultUi(result.userId, result.username) },
                                    isLoading = false,
                                )
                            }
                        },
                        onFailure = {
                            _uiState.update { it.copy(results = emptyList(), isLoading = false, hasError = true) }
                        },
                    )
                }
        }
    }

    private fun loadLikedProfiles() {
        viewModelScope.launch {
            getLikedProfilesUseCase.getCached().onSuccess { profiles ->
                if (profiles.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            results = profiles.map { profile ->
                                UserSearchResultUi(
                                    userId = profile.userId,
                                    username = profile.username.orEmpty(),
                                    displayName = profile.displayName,
                                    collectionSize = profile.bookCollection.size,
                                    sessionsThisMonth = profile.sessionsThisMonth,
                                )
                            },
                            isLoading = false,
                            hasError = false,
                        )
                    }
                }
            }

            _uiState.update { it.copy(isLoading = it.results.isEmpty(), hasError = false) }

            getLikedProfilesUseCase().fold(
                onSuccess = { profiles ->
                    _uiState.update {
                        it.copy(
                            results = profiles.map { profile ->
                                UserSearchResultUi(
                                    userId = profile.userId,
                                    username = profile.username.orEmpty(),
                                    displayName = profile.displayName,
                                    collectionSize = profile.bookCollection.size,
                                    sessionsThisMonth = profile.sessionsThisMonth,
                                )
                            },
                            isLoading = false,
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            results = if (it.results.isEmpty()) emptyList() else it.results,
                            isLoading = false,
                            hasError = it.results.isEmpty(),
                        )
                    }
                },
            )
        }
    }

    fun onQueryChange(query: String) {
        if (mode == UserSearchMode.LIKES) return
        _uiState.update { it.copy(query = query) }
        queryFlow.value = query
    }

    fun onUnlikeClick(userId: String) {
        if (mode != UserSearchMode.LIKES) return
        val currentUserId = getCurrentUserUseCase()?.uid ?: return
        val result = _uiState.value.results.firstOrNull { it.userId == userId } ?: return
        if (result.isLikeLoading) return

        _uiState.update { state ->
            state.copy(
                results = state.results.map {
                    if (it.userId == userId) it.copy(isLikeLoading = true) else it
                },
            )
        }
        viewModelScope.launch {
            toggleUserLikeUseCase(currentUserId, userId, liked = false).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(results = state.results.filterNot { it.userId == userId })
                    }
                },
                onFailure = {
                    _uiState.update { state ->
                        state.copy(
                            results = state.results.map {
                                if (it.userId == userId) it.copy(isLikeLoading = false) else it
                            },
                        )
                    }
                },
            )
        }
    }

    companion object {
        const val MODE_ARG = "mode"
    }
}
