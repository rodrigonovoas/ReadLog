package com.rodrigonovoa.readlog.ui.usersearch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigonovoa.readlog.domain.usecase.GetLikedProfilesUseCase
import com.rodrigonovoa.readlog.domain.usecase.SearchUsersUseCase
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
            _uiState.update { it.copy(isLoading = true, hasError = false) }

            getLikedProfilesUseCase().fold(
                onSuccess = { profiles ->
                    _uiState.update {
                        it.copy(
                            results = profiles.map { profile ->
                                UserSearchResultUi(
                                    userId = profile.userId,
                                    username = profile.username.orEmpty(),
                                )
                            },
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

    fun onQueryChange(query: String) {
        if (mode == UserSearchMode.LIKES) return
        _uiState.update { it.copy(query = query) }
        queryFlow.value = query
    }

    companion object {
        const val MODE_ARG = "mode"
    }
}
