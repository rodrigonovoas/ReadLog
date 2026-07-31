package com.rodrigonovoa.readlog.domain.model

data class UserProfileInfo(
    val userId: String = "",
    val likesCount: Int = 0,
    val sessionsThisMonth: Int = 0,
    val monthTimeSeconds: Long = 0L,
    val bookCollection: List<String> = emptyList(),
    val lastModified: Long = 0L,
    val displayName: String? = null,
    val username: String? = null,
    val followeds: List<String> = emptyList(),
)
