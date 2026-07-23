package com.rodrigonovoa.readlog.ui.userprofile

data class UserProfileUiState(
    val userName: String = "",
    val username: String = "",
    val likesCount: Int = 0,
    val weeklySessionsCount: Int = 0,
    val weeklyTimeLabel: String = "",
    val collectionBooks: List<UserProfileBook> = emptyList(),
    val isOwnProfile: Boolean = false,
    val isLiked: Boolean = false,
    val hasLikeError: Boolean = false,
)

data class UserProfileBook(
    val title: String,
)

val sampleUserProfileUiState = UserProfileUiState(
    userName = "Elena",
    username = "@elenalee",
    likesCount = 2940,
    weeklySessionsCount = 5,
    weeklyTimeLabel = "3h 40m",
    collectionBooks = listOf(
        UserProfileBook(title = "Cien años de soledad"),
        UserProfileBook(title = "Las palabras y las cosas"),
        UserProfileBook(title = "El nombre del viento"),
        UserProfileBook(title = "Norwegian Wood"),
    ),
    isOwnProfile = false,
    isLiked = true,
)
