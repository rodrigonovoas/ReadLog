package com.rodrigonovoa.readlog.ui.usersearch

data class UserSearchUiState(
    val query: String = "",
    val results: List<UserSearchResultUi> = emptyList(),
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
)

data class UserSearchResultUi(
    val userId: String,
    val username: String,
)

val sampleUserSearchUiState = UserSearchUiState(
    query = "elen",
    results = listOf(
        UserSearchResultUi(userId = "1", username = "elenalee"),
        UserSearchResultUi(userId = "2", username = "elena_ruiz"),
    ),
)
