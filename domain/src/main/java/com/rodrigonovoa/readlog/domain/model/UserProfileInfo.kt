package com.rodrigonovoa.readlog.domain.model

data class UserProfileInfo(
    val userId: String = "",
    val followersCount: Int = 0,
    val likesCount: Int = 0,
    val sessionsThisWeek: Int = 0,
    val weekTimeSeconds: Long = 0L,
    val bookCollection: List<String> = emptyList(),
    val lastModified: Long = 0L,
    val displayName: String? = null,
    val username: String? = null,
)
