package com.rodrigonovoa.readlog.ui.userprofile

data class UserProfileUiState(
    val userId: String = "",
    val userName: String = "",
    val username: String = "",
    val photoUrl: String? = null,
    val likesCount: Int = 0,
    val monthlySessionsCount: Int = 0,
    val monthlyTimeLabel: String = "",
    val collectionBooks: List<UserProfileBook> = emptyList(),
    val isOwnProfile: Boolean = false,
    val isLiked: Boolean = false,
    val hasLikeError: Boolean = false,
    val canLike: Boolean = true,
)

data class UserProfileBook(
    val title: String,
)

val sampleUserProfileUiState = UserProfileUiState(
    userId = "sample-uid",
    userName = "Elena",
    username = "@elenalee",
    likesCount = 2940,
    monthlySessionsCount = 5,
    monthlyTimeLabel = "3h 40m",
    collectionBooks = listOf(
        UserProfileBook(title = "Cien años de soledad"),
        UserProfileBook(title = "Las palabras y las cosas"),
        UserProfileBook(title = "El nombre del viento"),
        UserProfileBook(title = "Norwegian Wood"),
    ),
    isOwnProfile = false,
    isLiked = true,
)
